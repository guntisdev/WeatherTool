package server

import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object Server extends IOApp {
  private val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
  private val appRoutes = HttpRoutes.of[IO] {
    // http://localhost:3000/202304131030-202304132030/riga,liepaja/tempAvg
    case GET -> Root / timestampRange / cities / weatherParam =>
      // TODO proly better to Validated with chained errors
      val res = for {
        (from, to) <- timestampRange.split("-").toList
          .map(str => Try(LocalDateTime.parse(str, formatter)).toOption) match {
          case List(Some(from), Some(to)) => Some(from, to)
          case _ => None
        }
        cityList <- cities.split(",").toList match {
          case list => Some(list)
          case Nil => None
        }
        param <- if (weatherParam.isEmpty) None else Some(weatherParam)
      }
        // TODO yield to parser method which accepts those args
      yield s"Timestamp From: $from, Timestamp To: $to, Cities: ${cityList.mkString(", ")}, Weather Param: $param"

      res match {
        case Some(responseTxt) => Ok(responseTxt)
        case _ => BadRequest(s"Invalid request format")
      }
  }

  private val httpApp = Router("/" -> appRoutes).orNotFound

  override def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(3000, "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
