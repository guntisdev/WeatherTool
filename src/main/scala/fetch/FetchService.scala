package fetch

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import org.http4s.headers.Authorization
import com.typesafe.config.ConfigFactory

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext.global

object FetchService {
  private val config = ConfigFactory.load()
  private def getIOString(path: String): IO[String] =
    if (config.hasPath(path)) IO.pure(config.getString(path))
    else IO.raiseError(new RuntimeException(s"Missing configuration: $path"))

  private val basicCredentialsIO: IO[BasicCredentials] =
    for {
      username <- getIOString("username")
      password <- getIOString("password")
    } yield BasicCredentials(username, password)

  private val baseUrlIO: IO[Uri] = getIOString("url").map(Uri.unsafeFromString) // 20220831_1330.csv

  private def makeRequest(client: Client[IO], url: Uri): IO[Either[Throwable, (String, String)]] = {
    val fileName = url.path.toString()

    for {
      basicCredentials <- basicCredentialsIO
      request = Request[IO](Method.GET, url).withHeaders(Authorization(basicCredentials))
      result <- client.expect[String](request).redeemWith(
        error => IO(Left(error))
        // .flatTap(_ => IO.println(s"Request failed to url: $url with error: ${error.getMessage}")),
        ,
        fileContent => IO(Right((fileName, fileContent)))
        // .flatTap(_ => IO.println(s"Fetched: $fileName"))
      )
    } yield result
  }

  private def fetchFiles(fileNames: List[String]): IO[List[Either[Throwable, (String, String)]]] = {
    val IOUrls = baseUrlIO.map(baseUrl => fileNames.map(baseUrl / _))
    BlazeClientBuilder[IO](global).resource.use { client =>
      IOUrls.flatMap(_.traverse(url => makeRequest(client, url)))
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