package fetch

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.http4s.ember.client.EmberClientBuilder

import pureconfig._
import pureconfig.generic.auto._

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext.global

final case class WeatherServerConfig(
    username: String,
    password: String,
    url: String,
)

object FetchService {
  private val weatherServerConfig: WeatherServerConfig = ConfigSource.default.load[WeatherServerConfig] match {
    case Right(config) => config
    case Left(errors) => throw new RuntimeException(s"Unable to load config: $errors")
  }

  private val basicCredentials: BasicCredentials =
    BasicCredentials(weatherServerConfig.username, weatherServerConfig.password)

  private val baseUrl: IO[Uri] = IO(Uri.unsafeFromString(weatherServerConfig.url))

  private def makeRequest(client: Client[IO], url: Uri): IO[Either[Throwable, (String, String)]] = {
    val fileName = url.path.toString()
    val request = Request[IO](Method.GET, url).withHeaders(Authorization(basicCredentials))

    client.expect[String](request).redeemWith(
        error => IO(Left(error))
        // .flatTap(_ => IO.println(s"Request failed to url: $url with error: ${error.getMessage}")),
        ,
        fileContent => IO(Right((fileName, fileContent)))
        // .flatTap(_ => IO.println(s"Fetched: $fileName"))
      )
  }

  private def fetchFiles(fileNames: List[String]): IO[List[Either[Throwable, (String, String)]]] = {
    val IOUrls = baseUrl.map(baseUrl => fileNames.map(baseUrl / _))
    EmberClientBuilder.default[IO].build.use { client =>
      IOUrls.flatMap(_.traverse(url => makeRequest(client, url)))
    }
  }

  def fetchSingleFile(fileName: String): IO[(String, String)] = {
    fetchFiles(List(fileName)).flatMap { results =>
      results.headOption match {
        case Some(Right(result)) => IO.pure(result)
        case Some(Left(err)) => IO.raiseError(err)
        case None => IO.raiseError(new Exception("No file fetched"))
      }
    }
  }

  def fetchInRange(from: LocalDateTime, to: LocalDateTime): IO[List[Either[Throwable, (String, String)]]] = {
    val fileNames = FileNameService.generate(from, to)
    fetchFiles(fileNames)
  }

  def fetchFromDate(date: LocalDate): IO[List[Either[Throwable, (String, String)]]] = {
    val fileNames = FileNameService.generateFromDate(date)
    fetchFiles(fileNames)
  }
}