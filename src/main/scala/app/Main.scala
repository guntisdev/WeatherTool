package app

import cats.effect._
import cats.implicits.catsSyntaxTuple4Parallel
import data.DataService
import db.{DBConnection, PostgresService}
import fetch.{FetchService, FileFetchScheduler}
import server.Server

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    for {
      transactor <- DBConnection.transactor[IO]
      postgresService <- PostgresService.of(transactor)
      _ <- postgresService.createWeatherTable // create table if it does not exists

      fetch <- FetchService.of
      fileFetchScheduler <- FileFetchScheduler.of(postgresService, fetch)
      fetchCsvTask = fileFetchScheduler.run.compile.drain

      scheduler <- fetchDMI.Scheduler.of("Cleanup", List(1))
      cleanupTask = scheduler.scheduleTask(DataService.deleteOldForecasts()).compile.drain

      scheduler <- fetchDMI.Scheduler.of("Fetch Grib", List(2))
      fetchGrib <- fetchDMI.FetchService.of
      fetchGribTask = scheduler.scheduleTask(fetchGrib.fetchRecentForecasts()).compile.drain

      server <- Server.of(postgresService, fetch)
      serverTask = server.run

      exitCode <- (serverTask, fetchCsvTask, cleanupTask, fetchGribTask).parMapN((_, _, _, _) => ExitCode.Success)
    } yield exitCode
  }
}