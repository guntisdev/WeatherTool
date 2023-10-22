package db

import cats.effect._
import cats.effect.unsafe.implicits.global
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._

object Main {
    def transactor[F[_]: Async]: Transactor[F] = Transactor.fromDriverManager[F](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:5432/weather-tool",
      "postgres",
      "mysecretpassword"
    )

  def main(args: Array[String]): Unit = {
    val xa = transactor[IO]
    val result = for {
      postgresService <- PostgresService.of(xa)
      re <- postgresService.selectWeatherTable
    } yield re


//    val result = createWeatherTable(xa).unsafeRunSync()
//    val result = insertInWeatherTable(xa).unsafeRunSync()
//    val result = selectWeatherTable(xa).unsafeRunSync()
//    val result = dropWeatherTable(xa).unsafeRunSync()

    println(s"result: ${result.unsafeRunSync()}")
  }
}
