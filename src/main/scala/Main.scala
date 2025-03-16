import cats.effect._
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxTuple4Parallel
import data.DataService
import db.{DBConnection, PostgresService}
import fetch.csv.{FetchService, FileNameService}
import fetch.dmi
import scheduler.Scheduler
import server.Server

import java.io.{BufferedWriter, FileWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.duration.DurationInt

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val program = for {
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
      fetchGribTask = scheduler.scheduleTask("Fetch Grib", List(3), fetchGrib.fetchRecentForecasts()).compile.drain

      server <- Server.of(postgresService, dataService, fetchService)
      serverTask = server.run

      exitCode <- (serverTask, fetchCsvTask, cleanupTask, fetchGribTask).parMapN((_, _, _, _) => ExitCode.Success)
    } yield exitCode

    program.handleErrorWith { error =>
      IO.delay {
        println(s"Fatal error occurred: ${error.getMessage}")
        writeErrorToFile(error)
        error.printStackTrace()
      } *> IO.sleep(5.seconds) *> run(args)
    }
  }

  private def writeErrorToFile (error: Throwable) {
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"))
    val logPath = s"data/tmp/error_$timestamp.txt"

    fileWriterResource(logPath).use { writer =>
      IO(writer.write(formatError(error))) >>
        IO(writer.flush())
    }.unsafeRunSync()
  }

  private def formatError(error: Throwable): String = {
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    val stackTrace = error.getStackTrace.mkString("\n  ", "\n  ", "")
    s"[$timestamp] ERROR: ${error.getClass.getName}: ${error.getMessage}$stackTrace\n"
  }

  private def fileWriterResource(path: String): Resource[IO, BufferedWriter] =
    Resource.make {
      IO(new BufferedWriter(new FileWriter(path, true))) // append mode
    } { writer =>
      IO(writer.close()).handleErrorWith(e => IO(e.printStackTrace()))
    }
}