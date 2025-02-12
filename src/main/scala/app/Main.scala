package app

import cats.effect._
import cats.implicits.catsSyntaxTuple3Parallel
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
      schedulerTask = fileFetchScheduler.run.compile.drain


//      scheduler <- fetchDMI.Scheduler.of("Grib", List(3, 27, 39, 51))
//      simpleTask = IO.delay {
//        val timeNow = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
//        List(s"Task executed at $timeNow")
//      }
//      simpleScheduler = scheduler.scheduleTask(simpleTask).compile.drain


      scheduler <- fetchDMI.Scheduler.of("Cleanup", List(1))
      cleanupTask = DataService.deleteOldForecasts()
      cleanupScheduler = scheduler.scheduleTask(cleanupTask).compile.drain



      server <- Server.of(postgresService, fetch)
      serverTask = server.run

      exitCode <- (serverTask, schedulerTask, cleanupScheduler).parMapN((_, _, _) => ExitCode.Success)
    } yield exitCode
  }
}