package db

import cats.effect._
import cats.effect.unsafe.implicits.global
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter

object Main {
    def transactor[F[_]: Async]: Transactor[F] = Transactor.fromDriverManager[F](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/weather-tool",
      "postgres",
      "mysecretpassword"
    )

  def main(args: Array[String]): Unit = {
    val xa = transactor[IO]
//    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//    val localDate = LocalDate.parse("2023-12-10", formatter)
//    val result = for {
//      postgresService <- PostgresService.of(xa)
//      re <- postgresService.getDateFileNames(localDate)
//    } yield re

    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    val dateTime = LocalDateTime.parse("20231210_1300", formatter)
    val result = for {
            postgresService <- PostgresService.of(xa)
            re <- postgresService.getDateTimeEntries(dateTime)
          } yield re
    println(result.unsafeRunSync().toString())

    //    val result = createWeatherTable(xa).unsafeRunSync()
//    val result = insertInWeatherTable(xa).unsafeRunSync()
//    val result = selectWeatherTable(xa).unsafeRunSync()
//    val result = dropWeatherTable(xa).unsafeRunSync()

//    println(s"result: ${result.unsafeRunSync()}")
  }
}
