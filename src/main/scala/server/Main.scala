package server
import cats.effect._
import cats.implicits.catsSyntaxTuple2Parallel
import db.{DBService, DataService}
import fetch.{FetchService, FileFetchScheduler, StatefulFetchService}

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    for {
      dbService <- DBService.of
      dataService <- DataService.of(dbService)
      fetch <- FetchService.of
      statefulFetch <- StatefulFetchService.of(fetch)
      fileFetchScheduler <- FileFetchScheduler.of(dataService, statefulFetch)
      schedulerTask = fileFetchScheduler.run.compile.drain
      server <- Server.of(dataService, statefulFetch)
      serverTask = server.run
      exitCode <- (serverTask, schedulerTask).parMapN((_, _) => ExitCode.Success)
    } yield exitCode
  }
}