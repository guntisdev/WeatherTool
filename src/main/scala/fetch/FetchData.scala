package fetch

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import org.http4s.headers.Authorization

import com.typesafe.config.ConfigFactory

import java.nio.file.{Files, Paths}
import scala.concurrent.ExecutionContext.global

object FetchData extends IOApp.Simple {
  private val config = ConfigFactory.load()
  private val basicCredentials = BasicCredentials(config.getString("username"), config.getString("password"))
  private val baseUrl = Uri.unsafeFromString(config.getString("url")) // 20220831_1330.csv
//  private val baseUrl = Uri.unsafeFromString("https://jsonplaceholder.typicode.com/") // todos/1

  def saveToFile(fileName: String, content: String): IO[Unit] = {
    val path = Paths.get(s"data/$fileName")
    IO(Files.writeString(path, content)).attempt.flatMap {
      case Right(_) => IO(println(s"write: $fileName"))
      case Left(error) => IO(println(s"Write file '$fileName' failed with error: ${error.getMessage}"))
    }
  }

  def makeRequest(client: Client[IO], url: Uri): IO[Unit] = {
    val fileName = url.path.toString()
    for {
      request <- Request[IO](Method.GET, url)
        .withHeaders(Authorization(basicCredentials))
        .pure[IO]
      responseOrError <- client.expect[String](request).attempt
      _ <- responseOrError match {
        case Right(response) => saveToFile(fileName, response)
        case Left(error) => IO(println(s"Request failed to url: $url with error: ${error.getMessage}"))
      }
    } yield ()
  }

  def run: IO[Unit] = {
    val fileNames = FileName.generateLastNHours(10)
    //    fileNames.foreach(println)
    val urls = fileNames.map(baseUrl / _)
    BlazeClientBuilder[IO](global).resource.use { client =>
      urls.traverse(url => makeRequest(client, url)) // urls.map(...).sequence
    }
  }.as(ExitCode.Success)
}