package db

import cats.data.NonEmptyList
import cats.effect._
import doobie._
import doobie.implicits._

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import doobie.postgres.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object PostgresService {
  def of(transactor: Transactor[IO]): IO[PostgresService] = {
    Slf4jLogger.create[IO].map(logger => new PostgresService(transactor, logger))
  }
}

class PostgresService(transactor: Transactor[IO], log: Logger[IO]) {
  def getResourceContent(path: String): IO[String] = {
    val streamResource = Resource.make(IO(getClass.getResourceAsStream(path))) { stream =>
      IO(stream.close()).handleErrorWith(_ => IO.unit)
    }

    streamResource.use { stream =>
      IO(scala.io.Source.fromInputStream(stream).mkString)
    }
  }

  def createWeatherTable(): IO[Int] = {
    for {
      createTableSql <- getResourceContent("/db/create_weather_table.sql")
      result <- Update0(createTableSql, None).run.transact(transactor)
    } yield result
  }

  implicit val doubleOptionMeta: Meta[Option[Double]] = Meta[Double].imap(Option(_))(_.getOrElse(Double.NaN))

  def insertInWeatherTable(): IO[Int] = {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    val dateTime = LocalDateTime.parse("20230516_1500", formatter)

    val rigaZone = ZoneId.of("Europe/Riga")
    val zonedTime: ZonedDateTime = dateTime.atZone(rigaZone)

    // in pgAdmin run: ```SET TIMEZONE = 'Europe/Riga';```

    for {
      insertTableSql <- getResourceContent("/db/insert_weather_table.sql")
      result <- Update[(ZonedDateTime, String, Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], List[String])](
        insertTableSql
      ).run((zonedTime, "Rīga", Some(20.2), Some(8.8), Some(15.6), None, None, None, None, None, None, None, None, None, None, List("hail", "rain"))).transact(transactor)
    } yield result
  }

  def selectWeatherTable(): IO[List[(String, Option[Double])]] = {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    val rigaZone = ZoneId.of("Europe/Riga")
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
