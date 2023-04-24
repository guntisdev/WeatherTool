package parse

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Main {
  private def run: IO[Unit] = {
    println("================ start parser")

    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    val from = LocalDateTime.parse("20230414_2200", formatter)
    val to = LocalDateTime.parse("20230501_1230", formatter)

    for {
      lines <- db.DBService.getInRange(from, to)
      parsed <- IO.pure(Parser.queryData(lines, List("Liepāja", "Rēzekne", "randomstr"), AggregateMeteo.tempAvg))
      _ <- IO.println(parsed)
    } yield ()
  }

  def main(args: Array[String]): Unit = {
    run.unsafeRunSync()
  }

}
