package fetch

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits.toTraverseOps
import db.DBService
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.LocalDateTime

class FetchServiceSpec extends AnyFunSuite with Matchers {
//    def fetchInRange: IO[Unit] = {
//      val from = LocalDateTime.of(2023, 4, 28, 10, 0)
//      val to = LocalDateTime.of(2023, 4, 28, 13, 30)
//      for {
//        log <- Slf4jLogger.create[IO]
//        fetch <- FetchService.of
//        //      fetchResultEither <- fetch.fetchFromDate(LocalDate.of(2023, 4, 28)).attempt
//        fetchResultEither <- fetch.fetchInRange(from, to).attempt
//        fetchServiceError = fetchResultEither.left.toOption.map(e => s"FetchServiceError: ${e.getMessage}").toList
//        fetchResult = fetchResultEither.getOrElse(List.empty)
//        (fetchErrors, successDownloads) = fetchResult.partitionMap(identity)
//        dbService <- DBService.of
//        saveResults <- successDownloads.traverse { case (name, content) => dbService.save(name, content) }
//        (saveErrors, successSaves) = saveResults.partitionMap(identity)
//        successes = successDownloads.map(s => s"fetched: ${s._1}") ++ successSaves.map(s => s"saved: $s")
//        errors = fetchServiceError ++ fetchErrors.map(e => s"FetchError: ${e.getMessage}") ++ saveErrors.map(e => s"SaveError: ${e.getMessage}")
//        _ <- log.info(s"errors: $errors")
//        _ <- log.info(s"successes: $successes")
//      } yield (successes, errors)
//    }
//
//  def fetchSingleFile: IO[Unit] = {
//    for {
//      fetch <- FetchService.of
//      fetchResultEither <- fetch.fetchSingleFile("20230524_0030.csv").attempt
//      fetchResultEither <- fetch.fetchSingleFile("20230522_0130.csv").attempt
//      fetchServiceError = fetchResultEither.left.toOption.map(e => s"FetchServiceError: ${e.getMessage}").toList
//      fetchResult = fetchResultEither.flatMap(res => res.flatMap(aaa => {
//        println(s"fffffff: ${aaa._1}")
//        Right(aaa._1)
//      }))
//      //      _ = println(s"${fetchResult.map()}")
//    } yield ()
//  }
//
//  def main(args: Array[String]): Unit = {
//    run.unsafeRunSync()
//  }
}
