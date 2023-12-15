package db

import cats.data.NonEmptyList
import cats.effect._
import cats.effect.unsafe.implicits.global
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import parse.Aggregate.{AggregateKey, UserQuery}

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
    val from = LocalDateTime.parse("20231212_0000", formatter)
    val to = LocalDateTime.parse("20231214_2359", formatter)

    val query = UserQuery(NonEmptyList.of("Ainaži", "Rīga", "Kolka", "Vičaki"), "tempAvg", AggregateKey.Max, ChronoUnit.HOURS, from, to)

    val result = for {
            postgresService <- PostgresService.of(xa)
            re <- postgresService.query(query)
          } yield re
    println(result.unsafeRunSync().toString())

    //    val result = createWeatherTable(xa).unsafeRunSync()
//    val result = insertInWeatherTable(xa).unsafeRunSync()
//    val result = selectWeatherTable(xa).unsafeRunSync()
//    val result = dropWeatherTable(xa).unsafeRunSync()

//    println(s"result: ${result.unsafeRunSync()}")
  }
}
