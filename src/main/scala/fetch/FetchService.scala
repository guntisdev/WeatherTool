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
  // TODO wrap config in IO
  private val config = ConfigFactory.load()
  private val basicCredentials = BasicCredentials(config.getString("username"), config.getString("password"))
  private val baseUrl = Uri.unsafeFromString(config.getString("url")) // 20220831_1330.csv

  private def makeRequest(client: Client[IO], url: Uri): IO[Either[Throwable, (String, String)]] = {
    val fileName = url.path.toString()
    for {
      request <- Request[IO](Method.GET, url)
        .withHeaders(Authorization(basicCredentials))
        .pure[IO]
      result <- client.expect[String](request).redeemWith(
        error => IO(Left(error))
//          .flatTap(_ => IO.println(s"Request failed to url: $url with error: ${error.getMessage}"))
        ,
        fileContent => IO(Right((fileName, fileContent)))
//          .flatTap(_ => IO.println(s"Fetched: $fileName"))
      )
    } yield result
  }

  private def fetchFiles(fileNames: List[String]): IO[List[Either[Throwable, (String, String)]]] = {
    val urls = fileNames.map(baseUrl / _)
    BlazeClientBuilder[IO](global).resource.use { client =>
      urls.traverse(url => makeRequest(client, url))
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