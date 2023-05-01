package fetch

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.toTraverseOps
import db.DBService

import java.time.{LocalDate, LocalDateTime}

object Main {
  def run: IO[Unit] = {
    val from = LocalDateTime.of(2023, 4, 28, 10, 0)
    val to = LocalDateTime.of(2023, 4, 28, 13, 30)
    for {
//      fetchResultEither <- FetchService.fetchFromDate(LocalDate.of(2023, 4, 28)).attempt
      fetchResultEither <- FetchService.fetchInRange(from, to).attempt
      fetchServiceError = fetchResultEither.left.toOption.map(e => s"FetchServiceError: ${e.getMessage}").toList
      fetchResult = fetchResultEither.getOrElse(List.empty)
      (fetchErrors, successDownloads) = fetchResult.partitionMap(identity)
      saveResults <- successDownloads.traverse { case (name, content) => DBService.save(name, content) }
      (saveErrors, successSaves) = saveResults.partitionMap(identity)
      successes = successDownloads.map(s => s"fetched: ${s._1}") ++ successSaves.map(s => s"saved: $s")
      errors = fetchServiceError ++ fetchErrors.map(e => s"FetchError: ${e.getMessage}") ++ saveErrors.map(e => s"SaveError: ${e.getMessage}")
      _ <- IO.println(s"errors: $errors")
      _ <- IO.println(s"successes: $successes")
    } yield (successes, errors)
  }

  def main(args: Array[String]): Unit = {
    run.unsafeRunSync()
  }
}
