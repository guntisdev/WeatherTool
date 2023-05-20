package server
import cats.effect._
import cats.implicits.catsSyntaxTuple2Parallel
import db.DBService
import fetch.{FetchService, FileNameService}

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    for {
      dbService <- DBService.of
      fetchTask = FileNameService.generateCurrentHour.flatMap(FetchService.fetchSingleFile)
      scheduler = Scheduler.scheduleTask(fetchTask)
        .evalMap { case (name, content) =>
          IO(println(s"fetched: $name")) *>
            dbService.save(name, content).attempt.flatMap {
              case Right(savedName) => IO(println(s"File saved: $savedName"))
              case Left(err) => IO(println(s"Error: $err"))
            }
        }.compile.drain // Convert Stream[IO, Unit] to IO[Unit]

      server = Server.run // This is an IO[Server]

      exitCode <- (server, scheduler).parMapN((_, _) => ExitCode.Success)
    } yield exitCode
  }
}