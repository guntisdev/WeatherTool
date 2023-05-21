package server

import cats.effect._
import cats.implicits.toTraverseOps
import com.comcast.ip4s.IpLiteralSyntax
import db.DBService
import fetch.FetchService
import parse.{Parser, WeatherData}
import server.ValidateRoutes.{AggKey, CityList, DateTimeRange, ValidDate}
import io.circe.{Json, Printer}
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.{Router, staticcontent}
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.CORSConfig
import org.http4s.server.staticcontent.FileService
import org.http4s.ember.server.EmberServerBuilder
import io.circe.syntax._
import parse.Aggregate.AggregateValueImplicits.aggregateValueEncoder
import parse.Aggregate.{AggregateKey, UserQuery}
import org.http4s.circe.jsonEncoder

import scala.concurrent.duration.DurationInt


object Server {

  // Define the extension method `pretty` for Json
  implicit class JsonPrettyPrinter(json: Json) {
    def pretty: String = {
      val printer = Printer.spaces2.copy(dropNullValues = true)
      printer.print(json)
    }
  }

  private val apiRoutes = HttpRoutes.of[IO] {

    // http://0.0.0.0:8080/api/query/20230414_2200-20230501_1230/Liepāja,Rēzekne/tempMax/max
    case GET -> Root / "query" / DateTimeRange(from, to) / CityList(cities) / field / AggKey(key) =>
      DBService.of.flatMap(_.getInRange(from, to)
        .map(Parser.queryData(UserQuery(cities, field, key), _))
        .flatMap(result => Ok(result.asJson.pretty)))

    // http://0.0.0.0:8080/api/fetch/date/20230514
    case GET -> Root / "fetch" / "date" / ValidDate(date) =>
      val result = for {
        fetchResultEither <- FetchService.fetchFromDate(date).attempt
        fetchServiceError = fetchResultEither.left.toOption.map(e => s"FetchServiceError: ${e.getMessage}").toList
        fetchResult = fetchResultEither.getOrElse(List.empty)
        (fetchErrors, successDownloads) = fetchResult.partitionMap(identity)
        dbService <- DBService.of
        saveResults <- successDownloads.traverse { case (name, content) => dbService.save(name, content) }
        (saveErrors, successSaves) = saveResults.partitionMap(identity)
//        successes = successDownloads.map(s => s"fetched: ${s._1}") ++ successSaves.map(s => s"saved: $s")
        successes = successSaves
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

    // http://0.0.0.0:8080/api/show/all_dates
    case GET -> Root / "show" / "all_dates" =>
      DBService.of.flatMap(_.getDates().flatMap(dates =>
        Ok(dates.asJson.pretty)
      ))

    // http://0.0.0.0:8080/api/show/date/20230423
    case GET -> Root / "show" / "date" / ValidDate(date) =>
      DBService.of.flatMap(_.getDateFileNames(date).flatMap(fileNames =>
        Ok(fileNames.asJson.pretty)
      ))

    // http://0.0.0.0:8080/api/show/file/20230423_12:30.csv
    case GET -> Root / "show" / "file" / (fileName: String) =>
      DBService.of.flatMap(_.readFile(fileName).flatMap(content => Ok(content.asJson)))

    // http://0.0.0.0:8080/api/help
    case GET -> Root / "help" => {
      val host = "weather-tool.fly.dev"
      Ok(Json.obj(
        "aggregate fields" -> WeatherData.getKeys.asJson,
        "aggregate keys" -> AggregateKey.getKeys.asJson,
        "example urls" -> List(
          s"https://$host/api/query/20230414_2200-20230501_1230/Liepāja,Rēzekne/tempMax/max",
          s"https://$host/api/fetch/date/20230423",
          s"https://$host/api/show/all_dates",
          s"https://$host/api/show/date/20230423",
        ).asJson,
      ).pretty)
    }
  }

  val corsConfig = CORSConfig.default
    .withAnyOrigin(true)
    .withAnyMethod(true)
    .withAllowedMethods(Some(Set(Method.GET, Method.POST)))
    .withAllowCredentials(false)
    .withMaxAge(1.day)

  val apiRoutesCors = CORS(apiRoutes, corsConfig)

  private val httpApp = Router(
    "/" -> staticcontent.fileService[IO](FileService.Config("./web/dist")),
    "/api" -> apiRoutesCors
  ).orNotFound

  def run: IO[ExitCode] =
      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(httpApp)
        .build
        .useForever
}
