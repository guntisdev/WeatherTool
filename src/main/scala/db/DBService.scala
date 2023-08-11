package db

import cats.effect.{IO, Resource}
import cats.implicits.toTraverseOps
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.util.Try

object DBService {
  def of: IO[DBService] = {
    Slf4jLogger.create[IO].map(logger => new DBService(logger))
  }
}

class DBService(log: Logger[IO]) extends DataServiceTrait {
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
  private val dataPath = "./data"
  private val nonDuplicatedLines = 34 // takes only first 34 lines of data as rest after 'Zosēni' is duplicated

  private def readFileNames(path: String): IO[List[String]] =
    IO.blocking(new File(path).listFiles.toList.map(_.getName))
      .handleError(_ => List.empty)

  private def inRange(fileName: String, from: LocalDateTime, to: LocalDateTime): Boolean = {
    def fileToDateTime(fileName: String): Option[LocalDateTime] = {
      val dateString = fileName.split("\\.").head
      Try(LocalDateTime.parse(dateString, dateFormatter)).toOption
    }

    val fileDateTime = fileToDateTime(fileName.stripSuffix (".csv"))
    fileDateTime match {
      case Some (date) => date.plusSeconds (1).isAfter (from) && date.minusSeconds (1).isBefore (to)
      case None => false
    }
  }

  def save(fileName: String, content: String): IO[String] = {
    val path = Paths.get(s"$dataPath/$fileName")
    IO(Files.writeString(path, content))
      .attempt
      .flatMap {
        case Left(error) => log.error(s"Write file '$fileName' failed with error: ${error.getMessage}") *> IO.raiseError(error)
        case Right(_) => log.info(s"write: $fileName").as(fileName)
      }
  }

  def readFile(fileName: String): IO[List[String]] = {
    val file = new File(dataPath, fileName)
    val sourceResource = Resource.fromAutoCloseable(IO.blocking(Source.fromFile(file)))

    sourceResource
      .use(source => IO.blocking(source.getLines().take(nonDuplicatedLines).toList))
      .handleError(_ => List.empty)
  }

  def getInRange(from: LocalDateTime, to: LocalDateTime): IO[List[String]] = {
    for {
      fileNames <- readFileNames(dataPath)
        .map (_.filter (inRange (_, from, to)))
      fileLines <- fileNames.traverse(readFile)
    } yield fileLines.flatten
  }

  // dates in which we have saved data
  def getDates: IO[List[LocalDate]] = {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    for {
      fileNames <- readFileNames(dataPath)
      datesStr <- IO(fileNames.map(_.take(8)).distinct) // take yyyyMMdd
      dates <- datesStr.traverse { str =>
        IO(LocalDate.parse(str, formatter)).option
      }.map(_.flatten)
    } yield dates.sorted
  }

  def getDatesByMonths(monthList: List[LocalDate]): IO[List[LocalDate]] = {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val monthFormatter = DateTimeFormatter.ofPattern("yyyyMM")
    val monthStrList = monthList.map(_.format(monthFormatter))
    for {
      fileNames <- readFileNames(dataPath)
      datesStr <- IO(fileNames.map(_.take(8)).distinct) // take yyyyMMdd
      filteredDatesStr = datesStr.filter(date => monthStrList.contains(date.take(6)))
      dates <- filteredDatesStr.traverse { str =>
        IO(LocalDate.parse(str, dateFormatter)).option
      }.map(_.flatten)
    } yield dates.sorted
  }

  def getDateFileNames(date: LocalDate): IO[List[String]] = {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val dateStr: String = date.format(formatter)
    readFileNames(dataPath).map(_.filter(_.startsWith(dateStr)).sorted)
  }
}
