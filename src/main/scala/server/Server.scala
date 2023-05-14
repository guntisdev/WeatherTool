package server

import cats.effect._
import cats.implicits.toTraverseOps
import db.DBService
import fetch.FetchService
import parse.{Parser, WeatherData}
import server.ValidateRoutes.{AggKey, CityList, DateTimeRange, ValidDate}
import io.circe.{Json, Printer}
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import io.circe.syntax._
import parse.Aggregate.AggregateValueImplicits.aggregateValueEncoder
import parse.Aggregate.{AggregateKey, UserQuery}
import fs2.Stream


object Server {

  // Define the extension method `pretty` for Json
  implicit class JsonPrettyPrinter(json: Json) {
    def pretty: String = {
      val printer = Printer.spaces2.copy(dropNullValues = true)
      printer.print(json)
    }
  }

  private val appRoutes = HttpRoutes.of[IO] {

    // http://localhost:3000/query/20230414_2200-20230501_1230/Liepāja,Rēzekne/tempMax/max
    case GET -> Root / "query" / DateTimeRange(from, to) / CityList(cities) / field / AggKey(key) =>
      DBService.getInRange(from, to)
        .map(Parser.queryData(UserQuery(cities, field, key), _))
        .flatMap(result => Ok(result.asJson.pretty))

    // http://localhost:3000/fetch/date/20230423
    case GET -> Root / "fetch" / "date" / ValidDate(date) =>
      val result = for {
        fetchResultEither <- FetchService.fetchFromDate(date).attempt
        fetchServiceError = fetchResultEither.left.toOption.map(e => s"FetchServiceError: ${e.getMessage}").toList
        fetchResult = fetchResultEither.getOrElse(List.empty)
        (fetchErrors, successDownloads) = fetchResult.partitionMap(identity)
        saveResults <- successDownloads.traverse { case (name, content) => DBService.save(name, content) }
        (saveErrors, successSaves) = saveResults.partitionMap(identity)
        successes = successDownloads.map(s => s"fetched: ${s._1}") ++ successSaves.map(s => s"saved: $s")
        errors = fetchServiceError ++ fetchErrors.map(e => s"FetchError: ${e.getMessage}") ++ saveErrors.map(e => s"SaveError: ${e.getMessage}")
        _ <- IO.println(s"errors: $errors")
        _ <- IO.println(s"successes: $successes")
      } yield (successes, errors)

      result.flatMap { case (successes, errors) =>
        Ok(Json.obj(
            "errors" -> errors.asJson,
            "successes" -> successes.asJson
          ).pretty)
      }

    // http://localhost:3000/show/all_dates
    case GET -> Root / "show" / "all_dates" =>
      DBService.getDates().flatMap(dates =>
        Ok(dates.asJson.pretty)
      )

    // http://localhost:3000/show/date/20230423
    case GET -> Root / "show" / "date" / ValidDate(date) =>
      DBService.getDateFileNames(date).flatMap(fileNames =>
        Ok(fileNames.asJson.pretty)
      )

    // http://localhost:3000/help
    case GET -> Root / "help" =>
      Ok(Json.obj(
        "aggregate fields" -> WeatherData.getKeys.asJson,
        "aggregate keys" -> AggregateKey.getKeys.asJson,
        "example urls" -> List(
          "http://localhost:3000/query/20230414_2200-20230501_1230/Liepāja,Rēzekne/tempMax/max",
          "http://localhost:3000/fetch/date/20230423",
          "http://localhost:3000/show/all_dates",
          "http://localhost:3000/show/date/20230423",
        ).asJson,
      ).pretty)
  }

  private val httpApp = Router("/" -> appRoutes).orNotFound

  def run: Stream[IO, ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
}
