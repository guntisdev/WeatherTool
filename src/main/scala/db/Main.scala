package db

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.toTraverseOps
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter

object Main {
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")

  private def testGetInRange: IO[Unit] = {
    val from = LocalDateTime.parse("20230414_2200", dateFormatter)
    val to = LocalDateTime.parse("20230501_1230", dateFormatter)

    for {
      log <- Slf4jLogger.create[IO]
      dbService <- DBService.of
      lines <- dbService.getInRange(from, to)
      _ <- lines.traverse(log.info(_))
    } yield ()
  }

  private def testGetDates: IO[Unit] = {
    for {
      log <- Slf4jLogger.create[IO]
      dbService <- DBService.of
      dates <- dbService.getDates()
      _ <- log.info(s"$dates")
    } yield ()
  }

  private def testGetDate: IO[Unit] = {
    for {
      log <- Slf4jLogger.create[IO]
      dbService <- DBService.of
      dates <- dbService.getDateFileNames(LocalDate.of(2023, 5, 15))
      _ <- log.info(s"$dates")
    } yield ()
  }

  def main(args: Array[String]): Unit = {
    //    testGetInRange.unsafeRunSync()
    //    testGetDates.unsafeRunSync()
    testGetDate.unsafeRunSync()
  }
}
