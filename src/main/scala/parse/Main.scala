package parse

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import db.DBService
import io.circe.syntax.EncoderOps
import org.typelevel.log4cats.slf4j.Slf4jLogger
import parse.Aggregate.AggregateValueImplicits.aggregateValueEncoder
import parse.Aggregate.{AggregateKey, UserQuery}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Main {
  private def run: IO[Unit] = {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    val from = LocalDateTime.parse("20230515_0905", formatter)
    val to = LocalDateTime.parse("20230516_0942", formatter)
    val userQuery = UserQuery(List("Bauska", "Dagda", "Daugavgrīva", "Rīga"), "precipitation", AggregateKey.Sum)
//    val userQuery = UserQuery(List("Daugavgrīva"), "precipitation", AggregateKey.List)

    for {
      log <- Slf4jLogger.create[IO]
      dbService <- DBService.of
      lines <- dbService.getInRange(from, to)
      parsed <- IO.pure(Parser.queryData(userQuery, lines))
      _ <- log.info(parsed.asJson.toString)
    } yield ()
  }

  def main(args: Array[String]): Unit = {
    run.unsafeRunSync()
  }

}
