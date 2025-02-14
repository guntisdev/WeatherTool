package data

import cats.effect.IO
import cats.implicits.toTraverseOps
import fs2.io.file.{Files, Path}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import parse.grib.{Grib, GribParser}

import java.nio.file.Paths
import java.time.{ZoneId, ZoneOffset, ZonedDateTime}
import java.time.format.DateTimeFormatter
import scala.io.Source
import scala.util.Try

object DataService {
  def of: IO[DataService] = {
    for {
      logger <- Slf4jLogger.create[IO]
      service = new DataService(logger)
      _ <- service.init
    } yield service
  }
}


class DataService(log: Logger[IO]) {
  val BASE_FOLDER = "data"
  val GRIB_FOLDER = s"$BASE_FOLDER/grib"

  def init: IO[Unit] = {
    for {
      _ <- fs2.io.file.Files[IO].createDirectories(fs2.io.file.Path(BASE_FOLDER))
      _ <- fs2.io.file.Files[IO].createDirectories(fs2.io.file.Path(GRIB_FOLDER))
      _ <- log.info(s"Created directories: $BASE_FOLDER and $GRIB_FOLDER")
    } yield ()
  }

  def getFileList(): IO[List[String]] =
    Files[IO]
      .list(Path(GRIB_FOLDER))
      .map(_.toString)
      .filter(_.endsWith(".grib"))
      .map(_.replace(s"$GRIB_FOLDER/", ""))
      .compile
      .toList

  def getGribStucture(fileName: String): IO[List[Grib]] = {
    val filePath = Path(s"$GRIB_FOLDER/$fileName")

    Files[IO].exists(filePath).flatMap {
      case true => GribParser.parseFile(filePath)
      case false => IO.raiseError(new Exception(s"File not found: $fileName"))
    }
  }

  def getBinaryChunk(offset: Int, length: Int, fileName: String): IO[Array[Byte]] = {
    IO.blocking {
      val source = Source.fromFile(s"$GRIB_FOLDER/$fileName", "ISO-8859-1")
      try {
        source.slice(offset, offset + length).map(_.toByte).toArray
      } finally {
        source.close()
      }
    }
  }

  def getForecasts(): IO[List[(ZonedDateTime, ZonedDateTime)]] = {
    getFileList()
      .map { fileNameList =>
        fileNameList.flatMap(getTimeFromName)
      }
  }

  def deleteOldForecasts(maxHours: Int = 9): IO[List[String]] = {
    val nowUTC = ZonedDateTime.now(ZoneOffset.UTC)
    val ageThreshold = nowUTC.minusHours(maxHours)

    for {
      _ <- log.info("start cleanup")
      fileList <- getFileList()
      fileDateList = fileList.flatMap(fileName =>
        getTimeFromName(fileName).map(extracted => (fileName, extracted._1))
      )
      keepList = fileDateList.filter(_._2.isAfter(ageThreshold)).map(_._1)
      deleteList = fileList.filter(!keepList.contains(_))
      _ <- deleteList.traverse(name => Files[IO].delete(Path(s"$GRIB_FOLDER/${name}")))
      _ <- deleteList.traverse(name => log.info(s"delete: $name"))
    } yield deleteList
  }

  private def getTimeFromName(filename: String): Option[(ZonedDateTime, ZonedDateTime)] = {
    Try {
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmm'Z'").withZone(ZoneId.of("UTC"))
      val start = filename.slice(9, 25) // gets "2025-02-05T1500Z"
      val end = filename.slice(26, 42) // gets "2025-02-05T1800Z"
      (
        ZonedDateTime.parse(start, formatter).toInstant.atZone(ZoneOffset.UTC),
        ZonedDateTime.parse(end, formatter).toInstant.atZone(ZoneOffset.UTC)
      )
    }.toOption
  }

}
