package fetch

import cats.effect.IO
import db.DBService
import fs2.Stream
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger


object FileFetchScheduler {
  def of(dbService: DBService): IO[FileFetchScheduler] = {
    Slf4jLogger.create[IO].map {
      new FileFetchScheduler(dbService, _)
    }
  }
}
class FileFetchScheduler(dbService: DBService, log: Logger[IO]) {
  def run: Stream[IO, Unit] = {
    val fetchTask = FileNameService.generateCurrentHour.flatMap(FetchService.fetchSingleFile)
    Scheduler.scheduleTask(fetchTask)
      .evalMap { case (name, content) =>
        log.info(s"fetched: $name") *>
          dbService.save(name, content).attempt.flatMap {
            case Right(savedName) => log.info(s"saved: $savedName")
            case Left(err) => log.error(s"error: $err")
          }
      }
  }
}