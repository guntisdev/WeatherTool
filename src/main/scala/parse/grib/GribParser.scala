package parse.grib

import cats.effect._
import fs2.io.file.{Files, Path}

import java.nio.ByteBuffer
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{ZoneOffset, ZonedDateTime}
import scala.util.{Random, Try}

object GribParser {
  def parseFile(path: Path): IO[List[Grib]] = {
    def loop(ptr: Long, acc: List[Grib]): IO[List[Grib]] = {
      for {
        fileSize <- Files[IO].size(path)
        result <- if (ptr >= fileSize) {
          IO.pure(acc.reverse)
        } else {
          parseGrib(path, ptr).attempt.flatMap {
            case Right(grib) =>
              loop(ptr + grib.length, grib :: acc)
            case Left(error) =>
              // Just log the error and end parsing
              IO(println(s"Error parsing GRIB at position $ptr: ${error.getMessage}")) >>
              IO.pure(acc.reverse)
          }
        }
      } yield result
    }

    loop(0L, List.empty)
  }

  private def parseGrib(path: Path, ptr: Long): IO[Grib] = {
    for {
      res0 <- parse0(path, ptr)
      (version, discipline, gribLength, len0) = res0
      ptr1 = ptr + len0
      res1 <- parse1(path, ptr1)
      (referenceTime, len1) = res1
      ptr3 = ptr1 + len1
      res3 <- parse3(path, ptr3)
      (grid, len3) = res3
      ptr4 = ptr3 + len3
      res4 <- parse4(path, ptr4, discipline, referenceTime)
      (meteo, time, len4) = res4
//      _ <- IO.println(time)
      ptr5 = ptr4 + len4
      res5 <- parse5(path, ptr5)
      (conversion, bitsPerDataPoint, len5) = res5
      ptr6 = ptr5 + len5
      len6 <- parse6(path, ptr6)
      ptr7 = ptr6 + len6
      len7 <- parse7(path, ptr7)
      sections = List(
        GribSection(0, ptr, len0),
        GribSection(1, ptr1, len1),
        GribSection(3, ptr3, len3),
        GribSection(4, ptr4, len4),
        GribSection(5, ptr5, len5),
        GribSection(6, ptr6, len6),
        GribSection(7, ptr7, len7),
      )
      title = Codes.codesToString(meteo.discipline, meteo.category, meteo.product)
      grib = Grib(version, gribLength, title, grid, meteo, time, conversion, bitsPerDataPoint, sections)
    } yield grib
  }

  private def parse0(path: Path, ptr: Long): IO[(Int, Int, Long, Int)] = {
    val length = 16
    for {
      bytes <- readBytes(path, ptr, length)
      identifier = new String(bytes.take(4), "ASCII") // GRIB
      discipline = bytes(6)
      version = bytes(7)
      gribLength = ByteBuffer.wrap(bytes.slice(8, 16)).getLong
    } yield (version, discipline, gribLength, length)
  }

  private def parse1(path: Path, ptr: Long): IO[(ZonedDateTime, Int)] = {
    for {
      bytes <- readBytes(path, ptr, 64)
      length = ByteBuffer.wrap(bytes.slice(0, 4)).getInt
      year = ByteBuffer.wrap(bytes.slice(12, 14)).getShort
      month = bytes(14)
      day = bytes(15)
      hour = bytes(16)
      minute = bytes(17)
      second = bytes(18)
      referenceTime = Try(ZonedDateTime.of(
        year, month, day,
        hour, minute, second, 0,
        ZoneOffset.UTC // This is what 'Z' represents - UTC/Zero offset
      )).recoverWith { case _ => Try(extractDateTime(path))
      }.getOrElse(ZonedDateTime.of(2000, 1, 1, Random.nextInt(24), 0, 0, 0, ZoneOffset.UTC))
    } yield (referenceTime, length)
  }

  def extractDateTime(path: Path): ZonedDateTime = { // Path("data/harmonie_2025-02-28T1800Z_2025-02-28T1800Z.grib")
    val filename = path.fileName.toString
    val parts = filename.split("_")
    val dateTimeStr = parts(1)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmX")
    ZonedDateTime.parse(dateTimeStr, formatter)
  }

  private def parse3(path: Path, ptr: Long): IO[(GribGrid, Int)] = {
    for {
      bytes <- readBytes(path, ptr, 64)
      length = ByteBuffer.wrap(bytes.slice(0, 4)).getInt
      template = ByteBuffer.wrap(bytes.slice(12, 14)).getShort
      cols = ByteBuffer.wrap(bytes.slice(30, 34)).getInt
      rows = ByteBuffer.wrap(bytes.slice(34, 38)).getInt
      lambert1 = ByteBuffer.wrap(bytes.slice(34, 38)).getInt
      lambert2 = ByteBuffer.wrap(bytes.slice(38, 42)).getInt
      lambert = List(lambert1, lambert2)
    } yield (GribGrid(template, cols, rows, lambert), length)
  }

  private def parse4(path: Path, ptr: Long, discipline: Int, referenceTime: ZonedDateTime): IO[(MeteoParam, GribTime, Int)] = {
    for {
      bytes <- readBytes(path, ptr, 64)
      length = ByteBuffer.wrap(bytes.slice(0, 4)).getInt
      category = bytes(9) & 0xFF // to uint
      product = bytes(10) & 0xFF
      subtype = if (bytes(8) == 1) "now" else "period"
      levelType = bytes(22) & 0xFF
      levelValue = bytes(27) & 0xFF
      meteoParam = MeteoParam(discipline, category, product, subtype, levelType, levelValue)
      forecastTime = if (bytes(8) == 1) {
        val leadTime = ByteBuffer.wrap(bytes.slice(20, 22)).getShort
        referenceTime.plus(leadTime, ChronoUnit.HOURS)
      } else {
        val year = ByteBuffer.wrap(bytes.slice(37, 39)).getShort
        val month = bytes(39)
        val day = bytes(40)
        val hour = bytes(41)
        val minute = bytes(42)
        val second = bytes(43)
        ZonedDateTime.of(
          year, month, day,
          hour, minute, second, 0, // last 0 is nanos
          ZoneOffset.UTC // This is what 'Z' represents - UTC/Zero offset
        )
      }
      time = GribTime(referenceTime.format(formatter), forecastTime.format(formatter))
    } yield (meteoParam, time, length)
  }

  private def parse5(path: Path, ptr: Long): IO[(MeteoConversion, Int, Int)] = {
    for {
      bytes <- readBytes(path, ptr, 64)
      length = ByteBuffer.wrap(bytes.slice(0, 4)).getInt
      bitsPerDataPoint = bytes(19).toInt
      reference = ByteBuffer.wrap(bytes.slice(11, 15)).getFloat
      binaryScale = -(bytes(16) & 0xFF)
      decimalScale = bytes(18) & 0xFF
      conversion = MeteoConversion(reference, binaryScale, decimalScale)
    } yield (conversion, bitsPerDataPoint, length)
  }

  private def parse6(path: Path, ptr: Long): IO[Int] = {
    for {
      bytes <- readBytes(path, ptr, 64)
      length = ByteBuffer.wrap(bytes.slice(0, 4)).getInt
    } yield length
  }

  private def parse7(path: Path, ptr: Long): IO[Int] = {
    for {
      bytes <- readBytes(path, ptr, 64)
      length = ByteBuffer.wrap(bytes.slice(0, 4)).getInt
    } yield length
  }

  private def readBytes(path: Path, ptr: Long, len: Int): IO[Array[Byte]] = {
    Files[IO].readRange(path, 1024, ptr, ptr + len)
      .compile
      .to(Array)
  }
  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmX")
}