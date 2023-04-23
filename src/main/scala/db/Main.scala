package db

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.toTraverseOps
import db.DBService

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter

object Main {
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")

  private def testGetInRange: IO[Unit] = {
    val from = LocalDateTime.parse("20230414_2200", dateFormatter)
    val to = LocalDateTime.parse("20230501_1230", dateFormatter)

    for {
      lines <- DBService.getInRange(from, to)
      _ <- lines.traverse(IO.println)
    } yield ()
  }

  private def testGetDates: IO[Unit] = {
    for {
      dates <- DBService.getDates()
      _ <- IO.println(dates)
    } yield ()
  }

  private def testGetDate: IO[Unit] = {
    for {
      dates <- DBService.getDateFileNames(LocalDate.of(2023, 4, 23))
      _ <- IO.println(dates)
    } yield ()
  }

  def main(args: Array[String]): Unit = {
    println("----------------> db main")
    //    testGetInRange.unsafeRunSync()
    //    testGetDates.unsafeRunSync()
    testGetDate.unsafeRunSync()
  }
}
