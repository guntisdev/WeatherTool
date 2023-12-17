package app

import cats.effect._
import cats.implicits.catsSyntaxTuple2Parallel
import db.{DBConnection, PostgresService}
import fetch.{FetchService, FileFetchScheduler}
import server.Server

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    // postgres://weather_tool:XID547uldXliJBa@weather-tool-db.flycast:5432/weather_tool?sslmode=disable
    //    println(System.getenv("DATABASE_URL"))

    for {
      transactor <- DBConnection.transactor[IO]
      postgresService <- PostgresService.of(transactor)
      _ <- postgresService.createWeatherTable // create table if it does not exists

      fetch <- FetchService.of
      fileFetchScheduler <- FileFetchScheduler.of(postgresService, fetch)
      schedulerTask = fileFetchScheduler.run.compile.drain

      server <- Server.of(postgresService, fetch)
      serverTask = server.run

      exitCode <- (serverTask, schedulerTask).parMapN((_, _) => ExitCode.Success)
    } yield exitCode
  }
}