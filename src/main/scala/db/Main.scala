package db

import cats.data.NonEmptyList
import cats.effect._
import cats.effect.unsafe.implicits.global
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import io.circe.syntax.EncoderOps
import parse.Aggregate.{AggregateKey, UserQuery}
import parse.Aggregate.AggregateValueImplicits.aggregateValueEncoder
import parse.Aggregate.userQueryEncoder
import io.circe.{Json, Printer}
import io.circe.generic.auto._
import io.circe.syntax._
import parse.Aggregate

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

  implicit class JsonPrettyPrinter(json: Json) {
    def pretty: String = {
      val printer = Printer.spaces2.copy(dropNullValues = true)
      printer.print(json)
    }
  }
  private case class ResponseWrapper(result: Map[String, Option[Aggregate.AggregateValue]], query: UserQuery)

  def main(args: Array[String]): Unit = {
    val xa = transactor[IO]
//    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//    val localDate = LocalDate.parse("2023-12-10", formatter)
//    val result = for {
//      postgresService <- PostgresService.of(xa)
//      re <- postgresService.getDateFileNames(localDate)
//    } yield re

    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    val from = LocalDateTime.parse("20231210_0000", formatter)
    val to = LocalDateTime.parse("20231214_2359", formatter)
    val cities = NonEmptyList.of("Ainaži", "Rīga", "Kolka", "Vičaki")
//    val cities = NonEmptyList.of("Rīga")

    val query = UserQuery(cities, "tempMax", AggregateKey.List, ChronoUnit.DAYS, from, to)

    val re = for {
            postgresService <- PostgresService.of(xa)
            re <- postgresService.query(query)
          } yield re

    val jssson = re.map(result => ResponseWrapper(result, query))
        .map(responseWrapper => responseWrapper.asJson.pretty)

    println(jssson.unsafeRunSync())

    //    val result = createWeatherTable(xa).unsafeRunSync()
//    val result = insertInWeatherTable(xa).unsafeRunSync()
//    val result = selectWeatherTable(xa).unsafeRunSync()
//    val result = dropWeatherTable(xa).unsafeRunSync()

//    println(s"result: ${result.unsafeRunSync()}")
  }
}
