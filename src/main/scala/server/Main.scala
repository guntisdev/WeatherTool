package server
import cats.effect._
import db.DBService
import fetch.{FetchService, FileNameService}
import fs2.Stream

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val fetchTask = FileNameService.generateCurrentHour.flatMap(FetchService.fetchSingleFile)
    Stream(
      Server.run,
      Scheduler.scheduleTask(fetchTask)
        .evalMap { case (name, content) =>
          IO(println(s"fetched: $name")) *>
            DBService.save(name, content).attempt.flatMap {
              case Right(savedName) => IO(println(s"File saved: $savedName"))
              case Left(err) => IO(println(s"Error: $err"))
            }
        }
    ).parJoinUnbounded.compile.drain.as(ExitCode.Success)
  }
}