package fetch

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.toTraverseOps
import db.DBService

import java.time.{LocalDate, LocalDateTime}

object Main {
  def run: IO[Unit] = {
    val from = LocalDateTime.of(2023, 4, 23, 20, 0)
    val to = LocalDateTime.of(2023, 4, 23, 23, 30)
    for {
      fetched <- FetchService.fetchInRange(from, to)
//      fetched <- FetchService.fetchFromDate(LocalDate.of(2023, 4, 23))
      (errors, successfulDownloads) = fetched.partitionMap(identity)
      _ <- IO.println(s"Fetch errors: $errors")
      saveResults <- successfulDownloads.traverse { case (name, content) => DBService.save(name, content) }
      _ <- IO.println(s"Save results: $saveResults")
    } yield ()
  }

  def main(args: Array[String]): Unit = {
    run.unsafeRunSync()
  }
}
