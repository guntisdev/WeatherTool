package db

import cats.effect.{Async, IO}
import doobie.Transactor
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.error.ConfigReaderFailures

import scala.util.Try

case class PostgresConfig(url: String, username: String, password: String)

object DBConnection {
  private def getDatabaseUrl: String =
    sys.env.getOrElse("DATABASE_URL",
      "postgres://postgres:mysecretpassword@localhost:5432/weather-tool")

  private def parseUrl(url: String): Either[String, PostgresConfig] = {
    Try {
      val uri = new java.net.URI(if (url.startsWith("jdbc:")) url.substring(5) else url)
      val userInfo = uri.getUserInfo.split(":")
      val username = userInfo(0)
      val password = userInfo(1)
      val dbUrl = s"jdbc:postgresql://${uri.getHost}:${uri.getPort}${uri.getPath}"

      println(s"u: $username, p: $password, u: $url")

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
