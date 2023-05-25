package app

import cats.effect._
import cats.implicits.catsSyntaxTuple2Parallel
import db.{DBService, DataService}
import fetch.{FetchService, FileFetchScheduler}
import server.Server

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    for {
      dbService <- DBService.of
      dataService <- DataService.of(dbService)
      fetch <- FetchService.of
      fileFetchScheduler <- FileFetchScheduler.of(dataService, fetch)
      schedulerTask = fileFetchScheduler.run.compile.drain
      server <- Server.of(dataService, fetch)
      serverTask = server.run
      exitCode <- (serverTask, schedulerTask).parMapN((_, _) => ExitCode.Success)
    } yield exitCode
  }
}