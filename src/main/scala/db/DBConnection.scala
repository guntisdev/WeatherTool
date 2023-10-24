package db

import cats.effect.{Async, IO}
import doobie.Transactor
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.error.ConfigReaderFailures

case class PostgresConfig(username: String, password: String, url: String)

object DBConnection {
  private def loadPostgresConfig: Either[ConfigReaderFailures, PostgresConfig] = {
    ConfigSource.resources("postgres.conf").load[PostgresConfig]
  }

  def transactor[F[_] : Async]: IO[Transactor[F]] = {
    loadPostgresConfig match {
      case Right(config) => IO(Transactor.fromDriverManager[F](
        "org.postgresql.Driver",
        config.url,
        config.username,
        config.password
      ))
      case Left(errors) => IO.raiseError(new RuntimeException(s"Failed to load config: $errors"))
    }
  }
}
