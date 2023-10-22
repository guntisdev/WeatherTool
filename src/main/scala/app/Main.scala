package app

import cats.effect._
import cats.implicits.catsSyntaxTuple2Parallel
import db.{FileService, DataService}
import fetch.{FetchService, FileFetchScheduler}
import server.Server

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    for {
      fileService <- FileService.of
      dataService <- DataService.of(fileService)

      fetch <- FetchService.of
      fileFetchScheduler <- FileFetchScheduler.of(dataService, fetch)
      schedulerTask = fileFetchScheduler.run.compile.drain

      server <- Server.of(dataService, fetch)
      serverTask = server.run

      exitCode <- (serverTask, schedulerTask).parMapN((_, _) => ExitCode.Success)
    } yield exitCode
  }
}