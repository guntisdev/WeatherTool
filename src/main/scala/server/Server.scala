package server

import cats.effect._
import cats.implicits.toTraverseOps
import com.comcast.ip4s.IpLiteralSyntax
import data.DataService
import db.PostgresService
import fetch.csv.FileNameService
//import fetch.csv.FetchService
import fetch.lvgmc.FetchService
import fs2.io.file.{Files, Path}
import server.ValidateRoutes.{AggFieldList, AggKey, CityList, DateTimeRange, Granularity, ValidateDate, ValidateDateTime, ValidateInt, ValidateMonths, ValidateZonedDateTime}
import io.circe.{Encoder, Json, Printer}
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.{Router, staticcontent}
import org.http4s.server.middleware.CORS
import org.http4s.server.middleware.CORSConfig
import org.http4s.server.staticcontent.FileService
import org.http4s.ember.server.EmberServerBuilder
import io.circe.generic.auto._
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax._
import parse.csv.Aggregate.AggregateValueImplicits.aggregateValueEncoder
import parse.csv.Aggregate.userQueryEncoder
import parse.csv.Aggregate.{AggregateKey, UserQuery}
import org.http4s.circe.jsonEncoder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import parse.csv.Aggregate
import org.http4s.headers.`Content-Type`

import java.time.{ZoneOffset, ZonedDateTime}
import scala.concurrent.duration.DurationInt


object Server {
  def of(postgresService: PostgresService, dataService: DataService, fetch: FetchService): IO[Server] = {
    Slf4jLogger.create[IO].map {
      new Server(postgresService, dataService, fetch, _)
    }
  }
}

class Server(postgresService: PostgresService, dataService: DataService, fetch: FetchService, log: Logger[IO]) {
  implicit class JsonPrettyPrinter(json: Json) {
    def pretty: String = Printer.spaces2.copy(dropNullValues = true).print(json)
  }

  private case class ResponseWrapper(result: Map[String, Option[Aggregate.AggregateValue]], query: UserQuery)

  private val apiRoutes = HttpRoutes.of[IO] {
    // http://0.0.0.0:8080/api/show/lvgmc-forecast/Latvija_LTV_pilsetas_tekosa_dn.csv
    case GET -> Root / "show" / "lvgmc-forecast" / fileName =>
      fetch.fetchFile(fileName).flatMap(bytes =>
        Ok(bytes).map(_.withContentType(`Content-Type`(MediaType.text.csv)))
      )

    // http://0.0.0.0:8080/api/fetch/lvgmc/stations
    case GET -> Root / "fetch" / "lvgmc" / "stations" =>
      (
        for {
          fileName <- new FileNameService().generateCurrentHour
          stationDataStr <- fetch.fetchWeatherStations()
          _ <- postgresService.save(fileName, stationDataStr)
        } yield stationDataStr
      )
        .flatMap(content => Ok(content))
        .handleErrorWith(error =>
          InternalServerError(s"Failed to fetch stations: ${error.getMessage}")
        )

    // http://0.0.0.0:8080/api/show/grib-all-structure
    case GET -> Root / "show" / "grib-all-structure" =>
      dataService.getAllFileStructure().flatMap(gribList => Ok(gribList.asJson))

    // http://0.0.0.0:8080/api/show/grib-list
    case GET -> Root / "show" / "grib-list" =>
      dataService.getFileList().flatMap(fileList => Ok(fileList.asJson))

    // http://0.0.0.0:8080/api/show/grib-name/harmonie_2025-02-01T1500Z_2025-02-01T180000Z.grib
    case GET -> Root / "show" / "grib" / fileName =>
      dataService.getGribStucture(fileName).flatMap(response => Ok(response.asJson.pretty))

    case GET -> Root / "grib" / "binary-chunk" / ValidateInt(binaryOffset) / ValidateInt(binaryLength) / fileName =>
      dataService.getBinaryChunk(binaryOffset, binaryLength, fileName).flatMap(buffer => Ok(buffer))

    // http://0.0.0.0:8080/api/grib/delete-old-forecasts
    case GET -> Root / "grib" / "delete-old-forecasts" =>
      dataService.deleteOldForecasts().flatMap(result => Ok(result.asJson))

    // http://0.0.0.0:8080/api/debug/delete-tmp
    case GET -> Root / "debug" / "delete-tmp" =>
      dataService.deleteTmp().flatMap(result => Ok(result.asJson))

    // http://0.0.0.0:8080/api/debug/time
    case GET -> Root / "debug" / "time" =>
      val nowUTC = ZonedDateTime.now(ZoneOffset.UTC)
      val ageThreshold = nowUTC.minusHours(9)
      Ok(ageThreshold.toString)

    // http://0.0.0.0:8080/api/debug/folder-structure
    case GET -> Root / "debug" / "folder-structure" =>
      DebugUtils.getFolderStructure.flatMap(response => Ok(response.asJson))

    // http://0.0.0.0:8080/api/debug/file/error_2025-03-16_152201.txt
    case GET -> Root / "debug" / "file" / fileName =>
      val filePath = Path(s"data/tmp/$fileName")

      Files[IO].exists(filePath).flatMap {
        case true =>
          Files[IO].readUtf8(filePath)
            .compile
            .string
            .flatMap(content => Ok(content).map(_.withContentType(`Content-Type`(MediaType.text.plain, Charset.`UTF-8`))))
            .handleErrorWith(err => InternalServerError(s"Failed to read file: ${err.getMessage}"))
        case false =>
          NotFound(s"File not found: $fileName")
      }

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

//    // http://0.0.0.0:8080/api/fetch/date/20230514
//    case GET -> Root / "fetch" / "date" / ValidateDate(date) =>
//      val result = for {
//        fetchResultEither <- fetch.fetchFromDate(date).attempt
//        fetchServiceError = fetchResultEither.left.toOption.map(e => s"FetchServiceError: ${e.getMessage}").toList
//        fetchResult = fetchResultEither.getOrElse(List.empty)
//        (fetchErrors, successDownloads) = fetchResult.partitionMap(identity)
//        _ <- log.info(s"FETCHED SUCCESSFULLY files: ${successDownloads.size}")
//        saveResults <- successDownloads.traverse { case (name, content) => postgresService.save(name, content).attempt }
//        (saveErrors, successSaves) = saveResults.partitionMap(identity)
////        successes = successDownloads.map(s => s"fetched: ${s._1}") ++ successSaves.map(s => s"saved: $s")
//        successes = successSaves
//        errors = fetchServiceError ++ fetchErrors.map(e => s"FetchError: ${e.getMessage}") ++ saveErrors.map(e => s"SaveError: ${e.getMessage}")
//        _ <- log.error(s"errors: $errors")
//        _ <- log.info(s"successes: $successes")
//      } yield (successes, errors)
//
//      result.flatMap { case (successes, errors) =>
//        Ok(Json.obj(
//            "errors" -> errors.asJson,
//            "successes" -> successes.asJson
//          ).pretty)
//      }

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
    "/api" -> apiRoutesCors,

    // TODO rewrite in more generic way
    "/station" -> staticcontent.fileService[IO](FileService.Config("./web/dist")),
    "/cities" -> staticcontent.fileService[IO](FileService.Config("./web/dist")),
    "/latvia" -> staticcontent.fileService[IO](FileService.Config("./web/dist")),
    "/database" -> staticcontent.fileService[IO](FileService.Config("./web/dist")),
    "/harmonie" -> staticcontent.fileService[IO](FileService.Config("./web/dist")),
    "/lvgmc-forecast" -> staticcontent.fileService[IO](FileService.Config("./web/dist")),
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
