package fetch.lvgmc

import cats.effect._
import org.apache.commons.net.ftp.{FTP, FTPClient, FTPReply}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

final case class LVGMCServerConfig(
  username: String,
  password: String,
  url: String,
)

object FetchService {
  def of: IO[FetchService] = {
    Slf4jLogger.create[IO].map(logger => new FetchService(logger))
  }
}

class FetchService(log: Logger[IO]) {
  private val serverConfig: LVGMCServerConfig = (
    sys.env.get("LVGMC_USER"),
    sys.env.get("LVGMC_PASSWORD"),
    sys.env.get("LVGMC_URL")
  ) match {
    case (Some(user), Some(password), Some(url)) =>
      LVGMCServerConfig(user, password, url)
    case _ =>
      throw new RuntimeException("Unable to load lvgmc config: Missing required environment variables")
  }

  private def createFtpClient: Resource[IO, FTPClient] = {
    Resource.make {
      IO.blocking {
        val ftpClient = new FTPClient()
        ftpClient.connect(serverConfig.url)

        val reply = ftpClient.getReplyCode
        if (!FTPReply.isPositiveCompletion(reply)) {
          ftpClient.disconnect()
          throw new RuntimeException(s"FTP server refused connection, reply code: $reply")
        }

        val loggedIn = ftpClient.login(serverConfig.username, serverConfig.password)
        if (!loggedIn) {
          ftpClient.disconnect()
          throw new RuntimeException("Failed to login to FTP server")
        }

        ftpClient.enterLocalPassiveMode()
        ftpClient.setFileType(FTP.ASCII_FILE_TYPE)
        ftpClient
      }
    } { client =>
      IO.blocking {
        if (client.isConnected) {
          client.logout()
          client.disconnect()
        }
      }.handleErrorWith(e => log.error(e)("Error closing FTP connection"))
    }
  }

  private def retrieveFileAsBytes(ftpClient: FTPClient, remotePath: String): IO[Array[Byte]] = IO.blocking {
    val outputStream = new ByteArrayOutputStream()

    val success = ftpClient.retrieveFile(remotePath, outputStream)
    if (!success) {
      throw new RuntimeException(s"Failed to retrieve file: $remotePath, reply: ${ftpClient.getReplyString}")
    }

    outputStream.toByteArray()
  }.onError(e => log.error(e)(s"Error retrieving file $remotePath"))

  def fetchFile(fileName: String): IO[Array[Byte]] = {
    val remotePath = s"/ltv/tabulas/$fileName"

    createFtpClient.use { ftpClient =>
      for {
        _ <- log.info(s"Fetching file: $remotePath")
        content <- retrieveFileAsBytes(ftpClient, remotePath)
        _ <- log.info(s"Successfully fetched file: $fileName")
      } yield content
    }
  }

  def fetchWeatherStations(): IO[String] = {
    fetchFile("Latvija_faktiskais_laiks.csv").map(result => new String(result, StandardCharsets.UTF_8))
  }
}