package fetchDMI

import cats.effect._
import cats.implicits.toTraverseOps
import data.DataService
import fs2.io.file.{CopyFlag, CopyFlags, Files, Path}
import grib.GribParser
import org.http4s._
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.ZonedDateTime


final case class HarmonieServerConfig(
  api_key: String,
  url: String,
)

object FetchService {
  def of: IO[FetchService] = {
    Slf4jLogger.create[IO].map(logger => new FetchService(logger))
  }
}

class FetchService(log: Logger[IO]) {
  private val harmonieServerConfig: HarmonieServerConfig = (
    sys.env.get("HARMONIE_EDR_API_KEY"),
    sys.env.get("HARMONIE_EDR_URL"),
  ) match {
    case (Some(api_key), Some(url)) =>
      HarmonieServerConfig(api_key, url)
    case _ =>
      throw new RuntimeException("Unable to load harmonie config: Missing required environment variables")
  }

  /*
  https://dmigw.govcloud.dk/v1/forecastedr/collections/harmonie_dini_sf/grib
  ?parameter-name=temperature-2m
  &datetime=2025-01-28T00:00:00Z
  &api-key=b12d36c7-d7ba-4dca-9bc9-de0c9c27435f
   */

  private val baseUrl: IO[Uri] = IO(Uri.unsafeFromString(harmonieServerConfig.url))

  def fetchFromList(timeList: List[ZonedDateTime]): IO[List[String]] = {
    timeList.traverse { time =>
      fetchFromDateTime(time)
    }
  }

  private def fetchFromDateTime(time: ZonedDateTime): IO[String] = {
    EmberClientBuilder.default[IO].build.use { client =>
      for {
        base <- baseUrl
        queryParams = Query.fromPairs(
          // https://opendatadocs.dmi.govcloud.dk/Data/Forecast_Data_Weather_Model_HARMONIE_DINI_EDR
          "parameter-name" -> "temperature-2m,total-precipitation,precipitation-type,wind-speed,gust-wind-speed-10m,wind-10m-u,wind-10m-v",
          "datetime" -> time.toString,
          "api-key" -> harmonieServerConfig.api_key,
        )
        urlWithParams = Uri.unsafeFromString(s"${base.toString}?${queryParams.toString}")
        request = Request[IO](Method.GET, urlWithParams)

        tmpPath = Path(s"${DataService.FOLDER}/tmp.grib")
        _ <- client.stream(request)
          .flatMap(_.body)
          .through(Files[IO].writeAll(tmpPath))
          .compile
          .drain
        gribList <- GribParser.parseFile(tmpPath)
        gribTime = gribList.head.time
        fileName = Path(s"${DataService.FOLDER}/harmonie_${gribTime.referenceTime}_${gribTime.forecastTime}.grib".replace(":", ""))
        _ <- Files[IO].move(tmpPath, fileName, CopyFlags.apply(CopyFlag.ReplaceExisting))
        _ <- log.info(fileName.toString)
        fileNameStr = fileName.toString
      } yield fileNameStr
    }
  }
}