package server
import cats.effect._
import cats.implicits.catsSyntaxTuple2Parallel

import db.DBService
import fetch.FileFetchScheduler

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    for {
      dbService <- DBService.of
      fileFetchScheduler <- FileFetchScheduler.of(dbService)
      schedulerTask = fileFetchScheduler.run.compile.drain
      serverTask = Server.run
      exitCode <- (serverTask, schedulerTask).parMapN((_, _) => ExitCode.Success)
    } yield exitCode
  }
}