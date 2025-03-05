package data

import cats.effect.IO
import cats.effect.implicits.concurrentParTraverseOps
import cats.effect.kernel.Resource
import cats.effect.std.Semaphore
import cats.effect.unsafe.implicits.global
import cats.implicits._
import data.DataService.DeletionResult
import fs2.io.file.{Files, Path}
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import parse.grib.{Grib, GribParser}

import java.io.RandomAccessFile
import java.time.{ZoneId, ZoneOffset, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.util.Try

object DataService {
  case class DeletionResult(fileName: String, success: Boolean, errorMessage: Option[String])

  implicit val deletionResultEncoder: Encoder[DeletionResult] = deriveEncoder[DeletionResult]
  
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
  val TMP_FOLDER = s"$BASE_FOLDER/tmp"

  def init: IO[Unit] = {
    for {
      _ <- fs2.io.file.Files[IO].createDirectories(fs2.io.file.Path(BASE_FOLDER))
      _ <- fs2.io.file.Files[IO].createDirectories(fs2.io.file.Path(GRIB_FOLDER))
      _ <- fs2.io.file.Files[IO].createDirectories(fs2.io.file.Path(TMP_FOLDER))
      _ <- log.info(s"Created directories: $BASE_FOLDER, $GRIB_FOLDER, ${TMP_FOLDER}")
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

  def getAllFileStructure(): IO[List[Grib]] = {
    for {
      fileList <- getFileList()
      allStructure <- fileList.parTraverseN(4)(getGribStucture) // limit concurrency
    } yield allStructure.flatten
  }

  private val semaphore = Semaphore[IO](4).unsafeRunSync()

  private val blockingEC = ExecutionContext.fromExecutorService(
    Executors.newFixedThreadPool(4) // limit concurrency
  )

  def getBinaryChunk(offset: Int, length: Int, fileName: String): IO[Array[Byte]] = {
    val fileResource = Resource.make(
      IO.blocking(new RandomAccessFile(s"$GRIB_FOLDER/$fileName", "r"))
    )(file => IO.blocking(file.close()))

    val logMemory = IO {
      val runtime = Runtime.getRuntime
      val usedMemoryMB = (runtime.totalMemory - runtime.freeMemory) / 1024 / 1024
      val maxMemoryMB = runtime.maxMemory / 1024 / 1024
      println(s"Memory usage before reading $fileName: $usedMemoryMB MB / $maxMemoryMB MB max")
    }

    for {
//      _ <- logMemory
      result <- fileResource.use { file =>
        IO.blocking {
          val buffer = new Array[Byte](length)
          file.seek(offset)
          file.readFully(buffer)
          buffer
        }.evalOn(blockingEC)
      }
      _ <- IO { System.gc() }
    } yield result
  }

  def getForecasts(): IO[List[(ZonedDateTime, ZonedDateTime)]] = {
    getFileList()
      .map { fileNameList =>
        fileNameList.flatMap(getTimeFromName)
      }
  }

  def deleteOldForecasts(maxHours: Int = 9): IO[List[DeletionResult]] = {
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
      results <- deleteList.traverse { name =>
        val path = Path(s"$GRIB_FOLDER/${name}")
        Files[IO].delete(path).attempt.flatMap {
          case Right(_) => log.info(s"delete: $name").as(DeletionResult(name, true, None))
          case Left(error) => log.error(s"Failed to delete $name: ${error.getMessage}")
            .as(DeletionResult(name, false, Some(error.getMessage)))
        }
      }
    } yield results
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
