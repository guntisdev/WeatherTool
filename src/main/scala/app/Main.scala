package app

import cats.effect._
import cats.implicits.catsSyntaxTuple2Parallel
import db.Main.transactor
import db.{DataService, FileService, PostgresService}
import doobie.Transactor
import fetch.{FetchService, FileFetchScheduler}
import server.Server

object Main extends IOApp {

  def transactor[F[_] : Async]: Transactor[F] = Transactor.fromDriverManager[F](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5432/weather-tool",
    "postgres",
    "mysecretpassword"
  )
  def run(args: List[String]): IO[ExitCode] = {
    val xa = transactor[IO]

    for {
      postgresService <- PostgresService.of(xa)
      fileService <- FileService.of
      dataService <- DataService.of(fileService, postgresService)

      fetch <- FetchService.of
      fileFetchScheduler <- FileFetchScheduler.of(dataService, fetch)
      schedulerTask = fileFetchScheduler.run.compile.drain

      server <- Server.of(dataService, fetch)
      serverTask = server.run

      exitCode <- (serverTask, schedulerTask).parMapN((_, _) => ExitCode.Success)
    } yield exitCode
  }
}