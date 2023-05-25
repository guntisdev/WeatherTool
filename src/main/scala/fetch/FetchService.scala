package fetch

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig._
import pureconfig.generic.auto._

import java.time.{LocalDate, LocalDateTime}

final case class WeatherServerConfig(
    username: String,
    password: String,
    url: String,
)

object FetchService {
  def of: IO[FetchService] = {
    Slf4jLogger.create[IO].map(logger => new FetchService(new FileNameService, logger))
  }
}

class FetchService(fileNameService: FileNameService, log: Logger[IO]) {
  private val weatherServerConfig: WeatherServerConfig = ConfigSource.default.load[WeatherServerConfig] match {
    case Right(config) => config
    case Left(errors) => throw new RuntimeException(s"Unable to load config: $errors")
  }

  private val basicCredentials: BasicCredentials =
    BasicCredentials(weatherServerConfig.username, weatherServerConfig.password)

  private val baseUrl: IO[Uri] = IO(Uri.unsafeFromString(weatherServerConfig.url))

  private def makeRequest(client: Client[IO], url: Uri): IO[Either[Throwable, (String, String)]] = {
    val fileName = url.path.toString().tail
    val request = Request[IO](Method.GET, url).withHeaders(Authorization(basicCredentials))

    client.expect[String](request).redeemWith(
        error => IO(Left(error)) // <* log.error(s"Request failed to url: $url with error: ${error.getMessage}")
        ,
        fileContent => IO(Right((fileName, fileContent))) // <* log.info(s"Fetched: $fileName")
      )
  }

  private def fetchFiles(fileNames: List[String]): IO[List[Either[Throwable, (String, String)]]] = {
    val IOUrls = baseUrl.map(baseUrl => fileNames.map(baseUrl / _))
    EmberClientBuilder.default[IO].build.use { client =>
      IOUrls.flatMap(_.traverse(url => makeRequest(client, url)))
    }
  }

  def fetchSingleFile(fileName: String): IO[Either[Throwable, (String, String)]] = {
    fetchFiles(List(fileName)).map { results =>
      results.headOption match {
        case Some(Right(result)) =>
          log.info(s"fetched: $fileName").as(Right(result))
        case Some(Left(err)) =>
          log.error(s"failed fetch: $fileName with error: ${err.getMessage}").as(Left(err))
        case None =>
          log.error(s"failed fetch: $fileName").as(Left(new Exception("No file fetched")))
      }
    }.flatten
  }

  def fetchInRange(from: LocalDateTime, to: LocalDateTime): IO[List[Either[Throwable, (String, String)]]] = {
    val fileNames = fileNameService.generate(from, to)
    fetchFiles(fileNames)
  }

  def fetchFromDate(date: LocalDate): IO[List[Either[Throwable, (String, String)]]] = {
    val fileNames = fileNameService.generateFromDate(date)
    fetchFiles(fileNames)
  }
}