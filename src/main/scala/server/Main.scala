package server
import cats.effect._
import cats.implicits.catsSyntaxTuple2Parallel
import db.DBService
import fetch.{FetchService, FileFetchScheduler}

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    for {
      dbService <- DBService.of
      fetch <- FetchService.of
      fileFetchScheduler <- FileFetchScheduler.of(dbService, fetch)
      schedulerTask = fileFetchScheduler.run.compile.drain
      server <- Server.of(dbService, fetch)
      serverTask = server.run
      exitCode <- (serverTask, schedulerTask).parMapN((_, _) => ExitCode.Success)
    } yield exitCode
  }
}