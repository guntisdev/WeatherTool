package db

import cats.data.NonEmptyList
import cats.effect._
import cats.implicits.{catsSyntaxParallelTraverse1, toFoldableOps}
import doobie._
import doobie.implicits._

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime, OffsetDateTime, ZoneId, ZonedDateTime}
import doobie.postgres.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import parse.{Parser, WeatherStationData}

object PostgresService {
  def of(transactor: Transactor[IO]): IO[PostgresService] = {
    Slf4jLogger.create[IO].map(logger => new PostgresService(transactor, logger))
  }
}

class PostgresService(transactor: Transactor[IO], log: Logger[IO]) extends DataServiceTrait {
  private val rigaZone = ZoneId.of("Europe/Riga")
  // in pgAdmin run: ```SET TIMEZONE = 'Europe/Riga';```

  def save(fileName: String, content: String): IO[String] = {
    val strLines = content.split(System.lineSeparator()).toList
    val weatherStationData = strLines.flatMap(Parser.parseLine)
    insertInWeatherTable(weatherStationData)
      .attempt
      .flatMap {
        case Left(error) => log.error(s"Write db '$fileName' failed with error: ${error.getMessage}") *> IO.raiseError(error)
        case Right(rowCount) => log.info(s"write rows: $rowCount file: $fileName").as(fileName)
    }
  }

  def getDatesByMonths(monthList: List[LocalDate]): IO[List[LocalDate]] = {
    val monthYearPairs = monthList.map { localDate =>
      val month = localDate.atStartOfDay.atZone(rigaZone).getMonthValue
      val year = localDate.atStartOfDay.atZone(rigaZone).getYear
      (month, year)
    }

    val whereClauses = monthYearPairs.map {
      case (month, year) => fr"(EXTRACT(MONTH FROM dateTime) = $month AND EXTRACT(YEAR FROM dateTime) = $year)"
    }

    val combinedWhereClause = whereClauses.intercalate(fr" OR ")

    (
      fr"""
        SELECT DISTINCT DATE(dateTime)
        FROM weather
        WHERE """ ++ combinedWhereClause
    )
      .query[LocalDate]
      .to[List]
      .transact(transactor)
  }

  def readFile(fileName: String): IO[List[String]] = ???

  def getInRange(from: LocalDateTime, to: LocalDateTime): IO[List[String]] = ???

  def getDates: IO[List[LocalDate]] = ???

  def getDateTimeEntries(dateTime: LocalDateTime): IO[List[String]] = {
    val columnNames = List("City; TempMax; TempMin; TempAvg; Precipitation; WindAvg; WindMax; VisibilityMin; VisibilityAvg; SnowAvg; AtmPressure; DewPoint; Humidity; SunDuration; Phenomena")
    val result = fr"""
        SELECT city || ';' || COALESCE(tempmax::text, '' ) || ';' || COALESCE(tempmin::text, '' ) || ';' || COALESCE(tempavg::text, '' ) || ';' || COALESCE(precipitation::text, '' ) || ';' || COALESCE(windavg::text, '' ) || ';' || COALESCE(windmax::text, '' ) || ';' || COALESCE(tempmax::text, '' ) || ';' || COALESCE(visibilitymin::text, '' ) || ';' || COALESCE(visibilityavg::text, '' ) || ';' || COALESCE(snowavg::text, '' ) || ';' || COALESCE(atmpressure::text, '' ) || ';' || COALESCE(dewpoint::text, '' ) || ';' || COALESCE(humidity::text, '' ) || ';' || COALESCE(sunduration::text, '' )  || ';' || array_to_string(phenomena, ', ')
        FROM weather
        WHERE dateTime = $dateTime
        ORDER BY city
      """
        .query[String]
        .to[List]
        .transact(transactor)

    result.map(columnNames ++ _)
  }

  def getDateFileNames(date: LocalDate): IO[List[String]] = {
    val year = date.atStartOfDay.atZone(rigaZone).getYear
    val month = date.atStartOfDay.atZone(rigaZone).getMonthValue
    val day = date.atStartOfDay.atZone(rigaZone).getDayOfMonth
    val whereClauses = fr"(EXTRACT(DAY FROM dateTime) = $day AND EXTRACT(MONTH FROM dateTime) = $month AND EXTRACT(YEAR FROM dateTime) = $year)"
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HH00")

    (
      fr"""
          SELECT DISTINCT dateTime
          FROM weather
          WHERE """ ++ whereClauses ++
        fr"ORDER BY dateTime"
    )
      .query[OffsetDateTime]
      .to[List]
      .transact(transactor)
      .map { dateTimes =>
        dateTimes.map(dateTime =>
          dateTime.atZoneSameInstant(rigaZone).format(formatter)
        )
      }
  }

  def getResourceContent(path: String): IO[String] = {
    val streamResource = Resource.make(IO(getClass.getResourceAsStream(path))) { stream =>
      IO(stream.close()).handleErrorWith(_ => IO.unit)
    }

    streamResource.use { stream =>
      IO(scala.io.Source.fromInputStream(stream).mkString)
    }
  }

  def createWeatherTable: IO[Int] = {
    for {
      createTableSql <- getResourceContent("/db/create_weather_table.sql")
      result <- Update0(createTableSql, None).run.transact(transactor)
    } yield result
  }

  implicit val doubleOptionMeta: Meta[Option[Double]] = Meta[Double].imap(Option(_))(_.getOrElse(Double.NaN))

  def insertInWeatherTable(data: List[WeatherStationData]): IO[Int] = {
      getResourceContent("/db/insert_weather_table.sql").flatMap { insertTableSql =>
        val insertData = data.map(line => {
          val zonedTime: ZonedDateTime = line.timestamp.atZone(rigaZone)
          val w = line.weather
          (zonedTime, line.city, w.tempMax, w.tempMin, w.tempAvg, w.precipitation, w.windAvg, w.windMax, w.visibilityMin, w.visibilityAvg, w.snowAvg, w.atmPressure, w.dewPoint, w.humidity, w.sunDuration, w.phenomena)
        })

        Update[(ZonedDateTime, String, Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], List[String])](insertTableSql)
          .updateMany(insertData)
          .transact(transactor)
      }
  }

  def selectWeatherTable(): IO[List[(String, Option[Double])]] = {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    val from = LocalDateTime.parse("20230516_0100", formatter).atZone(rigaZone)
    val to = LocalDateTime.parse("20230516_1500", formatter).atZone(rigaZone)
    val citiesNel = NonEmptyList.of("Rīga", "Rēzekne")
    val granularity = "HOUR" // "HOUR", "DAY", "MONTH", "YEAR"
    val columnName = "tempMin"

    val baseQuery =
      fr"""
        SELECT
          city,
          MIN(""" ++ Fragment.const(columnName) ++ fr""") AS tempMin
        FROM weather
        WHERE
          """ ++ Fragments.in(fr"city", citiesNel) ++ fr"""
          AND dateTime BETWEEN $from AND $to
          AND """ ++ Fragment.const(columnName) ++fr""" IS NOT NULL
        GROUP BY city, EXTRACT(""" ++ Fragment.const(granularity) ++ fr""" FROM dateTime)
      """

    baseQuery
      .query[(String, Option[Double])]
      .to[List]
      .transact(transactor)
  }

  def dropWeatherTable(): IO[Int] = {
    for {
      dropTableSql <- getResourceContent("/db/drop_weather_table.sql")
      result <- Update0(dropTableSql, None).run.transact(transactor)
    } yield result
  }
}
