package db

import cats.effect.{Async, IO}
import doobie.Transactor
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.error.ConfigReaderFailures

import java.net.URI
import scala.util.Try

case class PostgresConfig(url: String, username: String, password: String)

object DBConnection {
  private def getDatabaseUrl: String =
    sys.env.getOrElse("DATABASE_URL", "postgres://postgres:mysecretpassword@localhost:5432/weather-tool")

  private def parseUrl(url: String): Either[String, PostgresConfig] = {
    Try {
      val raw = URI.create(url)

      val name = raw.getPath.substring(1)
      println("")
      val dbUrl = s"jdbc:postgresql://${raw.getHost}:${raw.getPort}${raw.getPath}?${raw.getQuery}"
      val username = raw.getUserInfo.split(":")(0)
      val password = raw.getUserInfo.split(":")(1)

      println(s"n: $name u: $username, p: $password, url: $url")

      PostgresConfig(dbUrl, username, password)
    }.toEither.left.map(_.getMessage)
  }

  def transactor[F[_] : Async]: IO[Transactor[F]] = {
    parseUrl(getDatabaseUrl) match {
      case Right(config) => IO(Transactor.fromDriverManager[F](
        "org.postgresql.Driver",
        config.url,
        config.username,
        config.password
      ))
      case Left(error) => IO.raiseError(new RuntimeException(s"Failed to parse DATABASE_URL: $error"))
    }
  }
}

//case class PostgresConfig(url: String, username: String, password: String)

//object DBConnection {
//  private def loadPostgresConfig: Either[ConfigReaderFailures, PostgresConfig] = {
//    ConfigSource.resources("postgres.conf").load[PostgresConfig]
//  }
//
//  def transactor[F[_] : Async]: IO[Transactor[F]] = {
//    loadPostgresConfig match {
//      case Right(config) => IO(Transactor.fromDriverManager[F](
//        "org.postgresql.Driver",
//        config.url,
//        config.username,
//        config.password
//      ))
//      case Left(errors) => IO.raiseError(new RuntimeException(s"Failed to load config: $errors"))
//    }
//  }
//}
