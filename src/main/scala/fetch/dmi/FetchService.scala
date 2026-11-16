package fetch.dmi

import cats.effect._
import cats.implicits._
import data.DataService
import fs2.io.file.{CopyFlag, CopyFlags, Files, Flags, Path}
import io.circe.Json
import io.circe.parser.decode
import org.http4s._
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import parse.grib.GribParser

import java.time.ZonedDateTime
import scala.concurrent.duration.DurationInt
import scala.util.Try


final case class HarmonieServerConfig(
  api_key: String,
  url: String,
)

object FetchService {
  def of(dataService: DataService): IO[FetchService] = {
    Slf4jLogger.create[IO].map(logger => new FetchService(dataService, logger))
  }
}

class FetchService(dataService: DataService, log: Logger[IO]) {
  private val stacConfig: HarmonieServerConfig = (
    sys.env.get("HARMONIE_STAC_API_KEY"),
    sys.env.get("HARMONIE_STAC_URL"),
  ) match {
    case (Some(api_key), Some(url)) =>
      HarmonieServerConfig(api_key, url)
    case _ =>
      throw new RuntimeException("Unable to load harmonie stac config: Missing required environment variables")
  }

  private val stacBaseUrl: IO[Uri] = IO(Uri.unsafeFromString(stacConfig.url))

  /*
  The EDR /grib endpoint was removed on 2026-11-09. Instead, download only the needed GRIB messages
  from the full STAC GRIB file, using its .index file (wgrib2 inventory) and HTTP Range requests.
  https://www.dmi.dk/friedata/dokumentation/forecast-data-stac-api
  Index line: 7:39798982:d=2026111612:TMP:2 m above ground:60 hour fcst:ENS=hi-res ctl
   */
  private val wantedMessages: Set[String] = Set( // "VAR:level" -> former EDR parameter name
    "TMP:2 m above ground", // temperature-2m
    "TPRATE:surface", // total-precipitation
    "var discipline=0 center=94 local_table=1 parmcat=1 parm=192:0 m above ground", // precipitation-type
    "WIND:10 m above ground", // wind-speed
    "GUST:10 m above ground", // gust-wind-speed-10m
    "UGRD:10 m above ground", // wind-10m-u
    "VGRD:10 m above ground", // wind-10m-v
    "SDWE:0 m above ground", // snow-depth-water-equivalent
  )

  /** byte ranges (start, inclusive end; None = until end of file) of wanted messages */
  private def selectMessages(index: String): List[(Long, Option[Long])] = {
    val lines = index.linesIterator.filter(_.nonEmpty).map(_.split(":")).toVector
    lines.zipWithIndex.collect {
      case (f, i) if wantedMessages.contains(s"${f(3)}:${f(4)}") &&
        !f(5).endsWith("min fcst") && (f(3) == "GUST" || !f(5).endsWith("max fcst")) => // skip TMP max/min
        (f(1).toLong, lines.lift(i + 1).map(_(1).toLong - 1))
    }.toList
  }

  def fetchFromList(forecastList: List[(ZonedDateTime, Uri)]): IO[List[String]] = {
    forecastList.traverse { case (_, gribUri) =>
      fetchFromGribFile(gribUri)
    }
  }

  private def fetchFromGribFile(gribUri: Uri): IO[String] = {
    val timeout = 60.seconds
    EmberClientBuilder.default[IO]
      .withTimeout(timeout)
      .withIdleConnectionTime(timeout)
      .build.use { client =>
      for {
        index <- client.expect[String](Uri.unsafeFromString(s"${gribUri.renderString}.index"))
        ranges = selectMessages(index)
        _ <- IO.raiseWhen(ranges.size != wantedMessages.size)(
          new RuntimeException(s"Expected ${wantedMessages.size} GRIB messages in index, found ${ranges.size}: $gribUri"))

        // TODO proly better to call dataService method than property
        tmpPath = Path(s"${dataService.TMP_FOLDER}/tmp.grib")
        _ <- Files[IO].deleteIfExists(tmpPath)
        _ <- ranges.traverse_ { case (start, end) =>
          val range = end.fold(headers.Range(start))(headers.Range(start, _))
          client.stream(Request[IO](Method.GET, gribUri).putHeaders(range))
            .flatMap { response =>
              if (response.status == Status.PartialContent) response.body
              else fs2.Stream.raiseError[IO](new RuntimeException(s"Range request failed (${response.status}): $gribUri"))
            }
            .through(Files[IO].writeAll(tmpPath, Flags.Append))
            .compile
            .drain
        }
        gribList <- GribParser.parseFile(tmpPath)
        gribTime = gribList.head.time
        fileName = Path(s"${dataService.GRIB_FOLDER}/harmonie_${gribTime.referenceTime}_${gribTime.forecastTime}.grib".replace(":", ""))
        _ <- Files[IO].move(tmpPath, fileName, CopyFlags.apply(CopyFlag.ReplaceExisting))
        fileSizeBytes <- Files[IO].size(fileName)
        fileSizeMB = fileSizeBytes.toDouble / (1024 * 1024)
        _ <- log.info(s" ${"%.1f".format(fileSizeMB)} MB - ${fileName.fileName}")
      } yield fileName.toString
    }
  }

  /**
   * get latest model run from STAC API
   * check local forecast grib files not to download them again
   * fetch those forecasts
   */
  def fetchRecentForecasts(): IO[List[String]] = {
    for {
      forecastList <- generateFetchList()
      resultList <- fetchFromList(forecastList)
      _ <- IO.println("finish grib downloads")
    } yield resultList
  }

  def generateFetchList(): IO[List[(ZonedDateTime, Uri)]] = {
    for {
      availableResult <- fetchAvailableForecasts()
      (modelRun, forecastList) = availableResult
      localForecasts <- dataService.getForecasts()
      toFetchList = forecastList.filter { case (dateTime, _) => !localForecasts.contains((modelRun, dateTime)) }
    } yield toFetchList
  }

  /** latest model run and its (forecast time, GRIB file url) list */
  def fetchAvailableForecasts(): IO[(ZonedDateTime, List[(ZonedDateTime, Uri)])] = {
    EmberClientBuilder.default[IO].build.use { client =>
      for {
        base <- stacBaseUrl
        queryParams = Query.fromPairs(
          "api-key" -> stacConfig.api_key,
        )
        urlWithParams = Uri.unsafeFromString(s"${base.toString}?${queryParams.toString}")
        request = Request[IO](Method.GET, urlWithParams).withHeaders(org.http4s.headers.Accept(org.http4s.MediaType.application.json))
        response <- client.expect[String](request)
        json <- IO.fromEither(decode[Json](response))
        result <- IO {
          val features = json.hcursor.downField("features").values.getOrElse(List.empty)

          val dateTimePairs = features.flatMap { feature =>
            for {
              properties <- feature.hcursor.downField("properties").focus
              modelRun <- properties.hcursor.downField("modelRun").as[String].toOption
              datetime <- properties.hcursor.downField("datetime").as[String].toOption
              parsedModelRun <- Try(ZonedDateTime.parse(modelRun)).toOption
              parsedDateTime <- Try(ZonedDateTime.parse(datetime)).toOption
              href <- feature.hcursor.downField("asset").downField("data").get[String]("href").toOption
              gribUri <- Uri.fromString(href).toOption
            } yield (parsedModelRun, parsedDateTime, gribUri)
          }

          val latestModelRun = dateTimePairs.map(_._1).max
          val datetimesForLatestRun = dateTimePairs
            .filter(_._1 == latestModelRun)
            .map { case (_, dateTime, gribUri) => (dateTime, gribUri) }
            .toList
            .sortBy(_._1)

          (latestModelRun, datetimesForLatestRun)
        }
      } yield result
    }
  }

}