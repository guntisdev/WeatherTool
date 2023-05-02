package parse

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.syntax.EncoderOps
import parse.Aggregate.AggregateValueImplicits.aggregateValueEncoder
import parse.Aggregate.{AggregateKey, UserQuery}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Main {
  private def run: IO[Unit] = {
    println("================ start parser")

    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    val from = LocalDateTime.parse("20230414_2200", formatter)
    val to = LocalDateTime.parse("20230501_1230", formatter)
    val userQuery = UserQuery(List("Liepāja", "Rēzekne", "randomstr"), "tempMax", AggregateKey.Max)

    for {
      lines <- db.DBService.getInRange(from, to)
      parsed <- IO.pure(Parser.queryData(userQuery, lines))
      _ <- IO.println(parsed.asJson)
      _ <- IO.println(None.asJson)
    } yield ()
  }

  def main(args: Array[String]): Unit = {
    run.unsafeRunSync()
  }

}
