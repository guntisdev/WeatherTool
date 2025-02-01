package fetchDMI

import cats.effect._
import fs2.io.file.{Files, Path}
import org.http4s._
import org.http4s.client.Client
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
  private def makeRequest(client: Client[IO], url: Uri) = {

  }

  def fetchFromDateTime(time: ZonedDateTime) = {
    EmberClientBuilder.default[IO].build.use { client =>
      for {
        base <- baseUrl
        queryParams = Query.fromPairs(
          "parameter-name" -> "temperature-2m",
          "datetime" -> time.toString,
          "api-key" -> harmonieServerConfig.api_key,
        )
        urlWithParams = Uri.unsafeFromString(s"${base.toString}?${queryParams.toString}")
        _ <- IO.println(urlWithParams)

        request = Request[IO](Method.GET, urlWithParams)

        _ <- Files[IO].createDirectories(Path("data"))

        _ <- client.stream(request)
          .flatMap(_.body)
          .through(Files[IO].writeAll(Path("data/tmp.grib")))
          .compile
          .drain
      } yield ()
    }
  }

}