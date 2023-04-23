package server

import cats.effect._
import cats.implicits.toTraverseOps
import db.DBService
import fetch.FetchService
import io.circe.{Json, Printer}
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import io.circe.syntax._
import io.circe.generic.auto._
import org.http4s.circe.jsonEncoder
import parse.{Meteo, Parser}

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
import scala.util.Try

object Server extends IOApp {
  private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
  private val appRoutes = HttpRoutes.of[IO] {
    // http://localhost:3000/query/20230414_2200-20230501_1230/Liepāja,Rēzekne/tempAvg
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
          val res = for {
            lines <- db.DBService.getInRange(from, to)
            parsedData <- parse.Parser.queryData(lines, cityList, aggregate)
          } yield parsedData

          Ok(res.map(_.toString()))
        }
        case _ => BadRequest(s"Invalid request format")
      }

    // http://localhost:3000/fetchDate/20230423
    case GET -> Root / "fetchDate" / dateStr => {
      val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
      val maybeDate = Try(LocalDate.parse(dateStr, dateFormatter)).toEither
      maybeDate match {
        case Right(date) => {
          val result = for {
            fetched <- FetchService.fetchFromDate(date)
            (fetchErrors, successDownloads) = fetched.partitionMap(identity)
            saved <- successDownloads.traverse { case (name, content) => DBService.save(name, content) }
            (saveErrors, successSaves) = saved.partitionMap(identity)
            successes = successDownloads.map(s => s"fetched: ${s._1}") ++ successSaves.map(s => s"saved: $s")
            errors = fetchErrors.map(e => s"FetchError: ${e.getMessage}") ++ saveErrors.map(e => s"SaveError: ${e.getMessage}")
          } yield (successes, errors)

          result.flatMap { case (successes, errors) =>
            val responseBody = Json.obj(
              "errors" -> errors.asJson,
              "successes" -> successes.asJson
            )
            val printer = Printer.spaces2.copy(dropNullValues = true)
            val prettyJson = printer.print(responseBody)
            Ok(prettyJson)
          }
        }
        case Left(_) => BadRequest("Invalid request format")
      }
    }

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
