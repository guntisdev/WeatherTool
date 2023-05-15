package db

import cats.effect.{IO, Resource}
import cats.implicits.toTraverseOps

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
import scala.io.Source
import scala.util.Try

object DBService {
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
  private val dataPath = "./data"

  private def readFile(fileName: String): IO[List[String]] = {
    val file = new File(dataPath, fileName)
    val sourceResource = Resource.fromAutoCloseable(IO(Source.fromFile(file)))
    sourceResource.use(source => IO(source.getLines().toList)).handleErrorWith(_ => IO.pure(List.empty))
  }

  private def readFileNames(path: String): IO[List[String]] =
    IO(new File(path).listFiles.toList.map(_.getName))
      .handleErrorWith(_ => IO.pure(List.empty))

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

  def getInRange(from: LocalDateTime, to: LocalDateTime): IO[List[String]] = {
    for {
      fileNames <- readFileNames(dataPath)
        .map (_.filter (inRange (_, from, to)))
      fileLines <- fileNames.traverse(readFile)
    } yield fileLines.flatten
  }

  def save(fileName: String, content: String): IO[Either[Throwable, String]] = {
    val path = Paths.get(s"$dataPath/$fileName")
    IO(Files.writeString(path, content))
      .redeemWith(
        error => IO(Left(error))
//          .flatTap(_ => IO.println(s"Write file '$fileName' failed with error: ${error.getMessage}"))
        ,
        _ => IO(Right(fileName))
//          .flatTap(_ => IO.println(s"write: $fileName"))
      )
  }

  // dates in which we have saved data
  def getDates(): IO[List[LocalDate]] = {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    for {
      fileNames <- readFileNames(dataPath)
      datesStr <- IO.pure(fileNames.map(_.take(8)).distinct) // take yyyyMMdd
      dates <- IO.pure(datesStr.flatMap(str => {
        Try(LocalDate.parse(str, formatter)).toOption
      }))
    } yield dates.sorted
  }

  def getDateFileNames(date: LocalDate): IO[List[String]] = {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val dateStr: String = date.format(formatter)
    readFileNames(dataPath).map(_.filter(_.startsWith(dateStr)).sorted)
  }

  def getFileContent(fileName: String): IO[String] = {
    val file = new File(dataPath, fileName)
    val sourceResource = Resource.fromAutoCloseable(IO(Source.fromFile(file)))
    sourceResource.use(source => IO(source.getLines().mkString("<br/>\n"))).handleErrorWith { error =>
      IO(println(s"Failed to read file: $error")) *> IO.pure("")
    }
  }
}
