package data

import cats.effect.IO
import fs2.io.file.{Files, Path}
import grib.{Grib, GribParser}

import java.time.{ZoneId, ZoneOffset, ZonedDateTime}
import java.time.format.DateTimeFormatter
import scala.io.Source
import scala.util.Try

object DataService {
  val FOLDER = "data"

  def getFileList(): IO[List[String]] =
    Files[IO]
      .list(Path(FOLDER))
      .map(_.toString)
      .filter(_.endsWith(".grib"))
      .map(_.replace(s"$FOLDER/", ""))
      .compile
      .toList

  def getGribStucture(fileName: String): IO[List[Grib]] = {
    val filePath = Path(s"$FOLDER/$fileName")

    Files[IO].exists(filePath).flatMap {
      case true => GribParser.parseFile(filePath)
      case false => IO.raiseError(new Exception(s"File not found: $fileName"))
    }
  }

  def getBinaryChunk(offset: Int, length: Int, fileName: String): IO[Array[Byte]] = {
    IO {
      val source = Source.fromFile(s"$FOLDER/$fileName", "ISO-8859-1")
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
