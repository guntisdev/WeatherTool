package server

import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import parse.{Meteo, Parser}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object Server extends IOApp {
  private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
  private val appRoutes = HttpRoutes.of[IO] {
    // http://localhost:3000/query/20230409_2200-20230501_1230/Liepāja,Rēzekne/tempAvg
    case GET -> Root / "query" / timestampRange / cities / aggregate =>
      // TODO proly better to Validated with chained errors
      val parsedArguments = for {
        (from, to) <- timestampRange.split("-").toList
          .map(str => Try(LocalDateTime.parse(str, formatter)).toOption) match {
          case List(Some(from), Some(to)) => Some(from, to)
          case _ => None
        }
        cityList <- cities.split(",").toList match {
          case list => Some(list)
          case Nil => None
        }
        aggregate <- Meteo.stringToAggregateParam(aggregate)

      }
      yield (from, to, cityList, aggregate)

      parsedArguments match {
        case Some((from, to, cityList, aggregate)) => {
          val resultData = Parser.queryData(from, to, cityList, aggregate)
          Ok(resultData.toString())
        }
        case _ => BadRequest(s"Invalid request format")
      }

    case GET -> Root / "fetch" / dateRange =>
      ???

    case GET -> Root / "show" / "fetched_dates" =>
      ???

    case GET -> Root / "show" / dateRange =>
      ???

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
