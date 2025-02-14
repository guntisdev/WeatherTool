package fetch.dmi

import cats.effect._
import cats.implicits.toTraverseOps
import data.DataService
import fs2.io.file.{CopyFlag, CopyFlags, Files, Path}
import io.circe.Json
import io.circe.parser.decode
import org.http4s._
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import parse.grib.GribParser

import java.time.ZonedDateTime
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
  private val edrConfig: HarmonieServerConfig = (
    sys.env.get("HARMONIE_EDR_API_KEY"),
    sys.env.get("HARMONIE_EDR_URL"),
  ) match {
    case (Some(api_key), Some(url)) =>
      HarmonieServerConfig(api_key, url)
    case _ =>
      throw new RuntimeException("Unable to load harmonie edr config: Missing required environment variables")
  }

  private val stacConfig: HarmonieServerConfig = (
    sys.env.get("HARMONIE_STAC_API_KEY"),
    sys.env.get("HARMONIE_STAC_URL"),
  ) match {
    case (Some(api_key), Some(url)) =>
      HarmonieServerConfig(api_key, url)
    case _ =>
      throw new RuntimeException("Unable to load harmonie stac config: Missing required environment variables")
  }

  /*
  https://dmigw.govcloud.dk/v1/forecastedr/collections/harmonie_dini_sf/grib
  ?parameter-name=temperature-2m
  &datetime=2025-01-28T00:00:00Z
  &api-key=b12d36c7-d7ba-4dca-9bc9-de0c9c27435f
   */

  private val edrBaseUrl: IO[Uri] = IO(Uri.unsafeFromString(edrConfig.url))
  private val stacBaseUrl: IO[Uri] = IO(Uri.unsafeFromString(stacConfig.url))

  def fetchFromList(timeList: List[ZonedDateTime]): IO[List[String]] = {
    timeList.traverse { time =>
      fetchFromDateTime(time)
    }
  }

  private def fetchFromDateTime(time: ZonedDateTime): IO[String] = {
    EmberClientBuilder.default[IO].build.use { client =>
      for {
        base <- edrBaseUrl
        queryParams = Query.fromPairs(
          // https://opendatadocs.dmi.govcloud.dk/Data/Forecast_Data_Weather_Model_HARMONIE_DINI_EDR
          "parameter-name" -> "temperature-2m,total-precipitation,precipitation-type,wind-speed,gust-wind-speed-10m,wind-10m-u,wind-10m-v",
          "datetime" -> time.toString,
          "api-key" -> edrConfig.api_key,
        )
        urlWithParams = Uri.unsafeFromString(s"${base.toString}?${queryParams.toString}")
        request = Request[IO](Method.GET, urlWithParams)

        // TODO proly better to call dataService method than property
        tmpPath = Path(s"${dataService.GRIB_FOLDER}/tmp.grib")
        _ <- client.stream(request)
          .flatMap(_.body)
          .through(Files[IO].writeAll(tmpPath))
          .compile
          .drain
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
      dateTimeList <- generateFetchList()
      resultList <- fetchFromList(dateTimeList)
      _ <- IO.println("finish grib downloads")
    } yield resultList
  }

  def generateFetchList(): IO[List[ZonedDateTime]] = {
    for {
      availableResult <- fetchAvailableForecasts()
      (modelRun, forecastDateList) = availableResult
      localForecasts <- dataService.getForecasts()
      toFetchList = forecastDateList.filter(dateTime => !localForecasts.contains((modelRun, dateTime)))
    } yield toFetchList
  }

  def fetchAvailableForecasts(): IO[(ZonedDateTime, List[ZonedDateTime])] = {
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
            } yield (parsedModelRun, parsedDateTime)
          }

          val latestModelRun = dateTimePairs.map(_._1).max
          val datetimesForLatestRun = dateTimePairs
            .filter(_._1 == latestModelRun)
            .map(_._2)
            .toList
            .sorted

          (latestModelRun, datetimesForLatestRun)
        }
      } yield result
    }
  }

}