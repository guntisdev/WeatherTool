package db

import cats.effect.{Async, IO}
import doobie.Transactor

import java.net.URI
import scala.util.Try

case class PostgresConfig(url: String, username: String, password: String)

object DBConnection {
  private def getDbName = sys.env.getOrElse("POSTGRES_DB", "")
  private def getDbUser = sys.env.getOrElse("POSTGRES_USER", "")
  private def getDbPassword = sys.env.getOrElse("POSTGRES_PASSWORD", "")

  private def getDatabaseUrl: String =
    sys.env.getOrElse("DATABASE_URL", s"postgres://${getDbUser}:${getDbPassword}@postgres:5432/${getDbName}")

  private def parseUrl(url: String): Either[String, PostgresConfig] = {
    Try {
      val raw = URI.create(url)

      val name = raw.getPath.substring(1)
      val dbUrl = s"jdbc:postgresql://${raw.getHost}:${raw.getPort}${raw.getPath}?${raw.getQuery}"
      val username = raw.getUserInfo.split(":")(0)
      val password = raw.getUserInfo.split(":")(1)

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