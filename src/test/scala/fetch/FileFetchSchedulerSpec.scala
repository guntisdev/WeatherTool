package fetch

import base.IOSuite
import cats.effect.{Clock, IO}
import cats.effect.kernel.Ref
import cats.implicits.catsSyntaxTuple3Semigroupal
import db.DBService
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.typelevel.log4cats.slf4j.Slf4jLogger
import fs2.Stream


class FileFetchSchedulerSpec extends AsyncWordSpec with Matchers with IOSuite {
  "FileFetchScheduler" should {
    "save into db" in runIO {
      for {
        refDb <- Ref.of[IO, Option[String]](None)
        refFetch <- Ref.of[IO, Option[(String, String)]](None)
        refScheduler <- Ref.of[IO, Option[Either[Throwable, (String, String)]]](None)
        services = Slf4jLogger.create[IO].flatMap { log =>
          val dbService = new DBService(log) {
            override def save(fileName: String, content: String): IO[Either[Throwable, String]] = {
              refDb.set(Some(fileName)).as(Right(fileName))
            }
          }
          val fileNameService = new FileNameService {
            override def generateCurrentHour(implicit clock: Clock[IO]): IO[String] = IO.pure("file_test")
          }
          val fetchService = new FetchService(fileNameService, log) {
            override def fetchSingleFile(fileName: String): IO[Either[Throwable, (String, String)]] = {
              refFetch.set(Some((fileName, "content"))).as(Right((fileName, "content")))
            }
          }
          val scheduler = new Scheduler(log) {
            override def scheduleTask(task: IO[Either[Throwable, (String, String)]]): Stream[IO, Either[Throwable, (String, String)]] = {
              Stream.eval(task).flatMap { result =>
                Stream.eval(refScheduler.set(Some(result))).as(result)
              }
            }
          }

          IO((dbService, fetchService, fileNameService, scheduler, log))
        }
        res <- services.flatMap { case (dbService, fetchService, fileNameService, scheduler, log) => {
          new FileFetchScheduler(dbService, fetchService, fileNameService, scheduler, log)
            .run.compile.drain *>
            (refDb.get, refFetch.get, refScheduler.get).tupled.map { case (db, fetch, scheduler) =>
              db shouldBe Some("file_test")
              fetch shouldBe Some(("file_test", "content"))
              scheduler shouldBe Some(Right(("file_test", "content")))
            }
          }
        }
      } yield res
    }
  }
}


