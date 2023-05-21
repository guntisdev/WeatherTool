package parse

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import db.DBService
import io.circe.syntax.EncoderOps
import parse.Aggregate.AggregateValueImplicits.aggregateValueEncoder
import parse.Aggregate.{AggregateKey, UserQuery}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Main {
  private def run: IO[Unit] = {
    println("================ start parser")

    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    val from = LocalDateTime.parse("20230515_0905", formatter)
    val to = LocalDateTime.parse("20230516_0942", formatter)
    val userQuery = UserQuery(List("Bauska", "Dagda", "Daugavgrīva", "Rīga"), "precipitation", AggregateKey.Sum)
//    val userQuery = UserQuery(List("Daugavgrīva"), "precipitation", AggregateKey.List)

    for {
      dbService <- DBService.of
      lines <- dbService.getInRange(from, to)
      parsed <- IO.pure(Parser.queryData(userQuery, lines))
      _ <- IO.println(parsed.asJson)
    } yield ()
  }

  def main(args: Array[String]): Unit = {
    run.unsafeRunSync()
  }

}
