package server

import cats.effect._
import cats.implicits.toTraverseOps
import com.comcast.ip4s.IpLiteralSyntax
import db.PostgresService
import fetch.FetchService
import grib.GribParser
import parse.Aggregate
import server.ValidateRoutes.{AggFieldList, AggKey, CityList, DateTimeRange, Granularity, ValidateDate, ValidateDateTime, ValidateMonths, ValidateZonedDateTime}
import io.circe.{Json, Printer}
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.{Router, staticcontent}
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.CORSConfig
import org.http4s.server.staticcontent.FileService
import org.http4s.ember.server.EmberServerBuilder
import io.circe.generic.auto._
import io.circe.syntax._
import parse.Aggregate.AggregateValueImplicits.aggregateValueEncoder
import parse.Aggregate.userQueryEncoder
import parse.Aggregate.{AggregateKey, UserQuery}
import org.http4s.circe.jsonEncoder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import fs2.io.file.Path

import scala.concurrent.duration.DurationInt


object Server {
  def of(postgresService: PostgresService, fetch: FetchService): IO[Server] = {
    Slf4jLogger.create[IO].map {
      new Server(postgresService, fetch, _)
    }
  }
}

class Server(postgresService: PostgresService, fetch: FetchService, log: Logger[IO]) {

  // Define the extension method `pretty` for Json
  implicit class JsonPrettyPrinter(json: Json) {
    def pretty: String = {
      val printer = Printer.spaces2.copy(dropNullValues = true)
      printer.print(json)
    }
  }

  private case class ResponseWrapper(result: Map[String, Option[Aggregate.AggregateValue]], query: UserQuery)

  private val apiRoutes = HttpRoutes.of[IO] {
    // http://0.0.0.0:8080/api/show/grib/2025-01-24T03:00:00Z
//    case GET -> Root / "show" / "grib" / ValidateZonedDateTime(referenceTime) =>
    case GET -> Root / "show" / "grib" =>
      // TODO change from hardcoded value to reference time and forecast time
      val fileName = "data/HARMONIE_DINI_SF_2025-01-24T030000Z_2025-01-26T010000Z.grib"
      GribParser.parseFile(Path(fileName)).flatMap(response => Ok(response.asJson.pretty))

    case GET -> Root / "show" / "gribName" => Ok("{\"fileName\":\"TODO replace this fake name\"}")

    // http://0.0.0.0:8080/api/query/city/Liepāja,Rēzekne/20230414_2200-20230501_1230/hour/tempMax/max
    case GET -> Root / "query" / "city" / CityList(cities) / DateTimeRange(from, to) / Granularity(granularity) / field / AggKey(key) =>
      val userQuery = UserQuery(cities, field, key, granularity, from, to)

      postgresService.query(userQuery)
        .map(result => ResponseWrapper(result, userQuery))
        .flatMap(responseWrapper => Ok(responseWrapper.asJson.pretty))

    // http://0.0.0.0:8080/api/query/city/Kolka/20230414_2200-20230501_1230/allFields
    case GET -> Root / "query" / "city" / (city: String) / DateTimeRange(from, to) / "allFields" =>
      postgresService.queryCityAllFields(city, from, to).flatMap(result => Ok(result.asJson))

    // http://0.0.0.0:8080/api/query/country/20230414_2200-20230501_1230/tempMax,tempMin,tempAvg,precipitation
    case GET -> Root / "query" / "country" / DateTimeRange(from, to) / AggFieldList(fieldList) =>
      postgresService.queryCountry(from, to, fieldList)
        .flatMap(result => Ok(result.asJson.pretty))

    // http://0.0.0.0:8080/api/fetch/date/20230514
    case GET -> Root / "fetch" / "date" / ValidateDate(date) =>
      val result = for {
        fetchResultEither <- fetch.fetchFromDate(date).attempt
        fetchServiceError = fetchResultEither.left.toOption.map(e => s"FetchServiceError: ${e.getMessage}").toList
        fetchResult = fetchResultEither.getOrElse(List.empty)
        (fetchErrors, successDownloads) = fetchResult.partitionMap(identity)
        _ <- log.info(s"FETCHED SUCCESSFULLY files: ${successDownloads.size}")
        saveResults <- successDownloads.traverse { case (name, content) => postgresService.save(name, content).attempt }
        (saveErrors, successSaves) = saveResults.partitionMap(identity)
//        successes = successDownloads.map(s => s"fetched: ${s._1}") ++ successSaves.map(s => s"saved: $s")
        successes = successSaves
        errors = fetchServiceError ++ fetchErrors.map(e => s"FetchError: ${e.getMessage}") ++ saveErrors.map(e => s"SaveError: ${e.getMessage}")
        _ <- log.error(s"errors: $errors")
        _ <- log.info(s"successes: $successes")
      } yield (successes, errors)

      result.flatMap { case (successes, errors) =>
        Ok(Json.obj(
            "errors" -> errors.asJson,
            "successes" -> successes.asJson
          ).pretty)
      }

    // http://0.0.0.0:8080/api/show/months/202304,202305,202306
    case GET -> Root / "show" / "months" / ValidateMonths(monthList) =>
      postgresService.getDatesByMonths(monthList).flatMap(dates =>
        Ok(dates.asJson.pretty)
      )

    // http://0.0.0.0:8080/api/show/date/20230423
    case GET -> Root / "show" / "date" / ValidateDate(date) =>
      postgresService.getDateFileNames(date).flatMap(fileNames =>
        Ok(fileNames.asJson.pretty)
      )

    // http://0.0.0.0:8080/api/show/datetime/20230423_1300
    case GET -> Root / "show" / "datetime" / ValidateDateTime(datetime) =>
      postgresService.getDateTimeEntries(datetime).flatMap(content => Ok(content.asJson))
  }

  private val corsConfig = CORSConfig.default
    .withAnyOrigin(true)
    .withAnyMethod(true)
    .withAllowedMethods(Some(Set(Method.GET, Method.POST)))
    .withAllowCredentials(false)
    .withMaxAge(1.day)

  private val apiRoutesCors = CORS(apiRoutes, corsConfig)

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
