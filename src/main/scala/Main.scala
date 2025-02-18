import cats.effect._
import cats.implicits.catsSyntaxTuple4Parallel
import data.DataService
import db.{DBConnection, PostgresService}
import fetch.csv.{FetchService, FileNameService}
import fetch.dmi
import scheduler.Scheduler
import server.Server

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    for {
      transactor <- DBConnection.transactor[IO]
      postgresService <- PostgresService.of(transactor)
      _ <- postgresService.createWeatherTable // create table if it does not exists

      scheduler <- Scheduler.of
      dataService <- DataService.of
      fetchService <- FetchService.of

      fetchCsvList = new FileNameService().generateCurrentHour
        .flatMap(fetchService.fetchSingleFile)
        .flatMap {
          case Right((name, content)) => postgresService.save(name, content)
          case Left(_) => IO.unit // ignore error
        }
      fetchCsvTask = scheduler.scheduleTask("Fetch CSV", List(31), fetchCsvList).compile.drain

      cleanupTask = scheduler.scheduleTask("Cleanup", List(1), dataService.deleteOldForecasts()).compile.drain

      fetchGrib <- dmi.FetchService.of(dataService)
      fetchGribTask = scheduler.scheduleTask("Fetch Grib", List(2), fetchGrib.fetchRecentForecasts()).compile.drain

      server <- Server.of(postgresService, dataService, fetchService)
      serverTask = server.run

      exitCode <- (serverTask, fetchCsvTask, cleanupTask, fetchGribTask).parMapN((_, _, _, _) => ExitCode.Success)
    } yield exitCode
  }
}