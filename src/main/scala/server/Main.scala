package server
import cats.effect._
import cats.implicits.catsSyntaxTuple2Parallel
import db.DBService
import fetch.{FetchService, FileFetchScheduler, StatefulFetchService}

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    for {
      dbService <- DBService.of
      fetch <- FetchService.of
      statefulFetch <- StatefulFetchService.of(fetch)
      fileFetchScheduler <- FileFetchScheduler.of(dbService, statefulFetch)
      schedulerTask = fileFetchScheduler.run.compile.drain
      server <- Server.of(dbService, statefulFetch)
      serverTask = server.run
      exitCode <- (serverTask, schedulerTask).parMapN((_, _) => ExitCode.Success)
    } yield exitCode
  }
}