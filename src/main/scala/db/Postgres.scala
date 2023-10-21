package db

import cats.effect._
import cats.effect.unsafe.implicits.global
import doobie._
import doobie.implicits._

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import doobie.postgres.implicits._

case class SqlContent(value: String)

object Postgres {
  def transactor[F[_]: Async]: Transactor[F] = Transactor.fromDriverManager[F](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5432/weather-tool",
    "postgres",
    "mysecretpassword"
  )

  def getResourceContent(path: String): IO[String] = {
    val streamResource = Resource.make(IO(getClass.getResourceAsStream(path))) { stream =>
      IO(stream.close()).handleErrorWith(_ => IO.unit)
    }

    streamResource.use { stream =>
      IO(scala.io.Source.fromInputStream(stream).mkString)
    }
  }

//  def findUserById(userId: Int)(implicit xa: Transactor[IO]): IO[Option[User]] = {
//    sql"SELECT id, name FROM users WHERE id = $userId"
//      .query[User]
//      .option
//      .transact(xa)
//  }

  def createWeatherTable(implicit xa: Transactor[IO]): IO[Int] = {
    for {
      createTableSql <- getResourceContent("/db/create_weather_table.sql")
      result <- Update0(createTableSql, None).run.transact(xa)
    } yield result
  }

  implicit val doubleOptionMeta: Meta[Option[Double]] = Meta[Double].imap(Option(_))(_.getOrElse(Double.NaN))

  def insertInWeatherTable(xa: Transactor[IO]): IO[Int] = {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    val dateTime = LocalDateTime.parse("20230516_1500", formatter)

    val rigaZone = ZoneId.of("Europe/Riga")
    val zonedTime: ZonedDateTime = dateTime.atZone(rigaZone)

    // in pgAdmin run: ```SET TIMEZONE = 'Europe/Riga';```

    for {
      insertTableSql <- getResourceContent("/db/insert_weather_table.sql")
      result <- Update[(ZonedDateTime, String, Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], Option[Double], List[String])](
        insertTableSql
      ).run((zonedTime, "Rīga", Some(20.2), Some(8.8), Some(15.6), None, None, None, None, None, None, None, None, None, None, List("hail", "rain"))).transact(xa)
    } yield result
  }

  def dropWeatherTable(implicit xa: Transactor[IO]): IO[Int] = {
    for {
      dropTableSql <- getResourceContent("/db/drop_weather_table.sql")
      result <- Update0(dropTableSql, None).run.transact(xa)
    } yield result
  }

  def main(args: Array[String]): Unit = {
    val xa = transactor[IO]

//    val result = createWeatherTable(xa).unsafeRunSync()
    val result = insertInWeatherTable(xa).unsafeRunSync()
//    val result = dropWeatherTable(xa).unsafeRunSync()

    println(s"result: $result")
  }
}
