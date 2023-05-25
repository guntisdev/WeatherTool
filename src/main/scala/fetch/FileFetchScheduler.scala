package fetch

import cats.effect.IO
import db.DataServiceTrait
import fs2.Stream
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger


object FileFetchScheduler {
  def of(dataService: DataServiceTrait, fetch: FetchService): IO[FileFetchScheduler] = {
    Scheduler.of.flatMap { scheduler =>
      Slf4jLogger.create[IO].map {
        new FileFetchScheduler(dataService, fetch, new FileNameService(), scheduler, _)
      }
    }
  }
}

class FileFetchScheduler(dataService: DataServiceTrait, fetch: FetchService, fileNameService: FileNameService, scheduler: Scheduler, log: Logger[IO]) {
  def run: Stream[IO, Unit] = {
    val fetchTask = fileNameService.generateCurrentHour.flatMap(fetch.fetchSingleFile)
    scheduler.scheduleTask(fetchTask)
      .evalMap {
        case Left(fetchErr) =>
          log.error(s"Fetch error: $fetchErr")
        case Right((name, content)) =>
          dataService.save(name, content).attempt.flatMap {
            case Left(err) => log.error(s"error: $err")
            case Right(saveResult) => saveResult match {
              case Left(err) => log.error(s"error: $err")
              case Right(savedName) => log.info(s"saved: $savedName")
            }
          }
      }
  }
}