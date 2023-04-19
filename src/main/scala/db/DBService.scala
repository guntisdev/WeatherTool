package db

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import cats.implicits.toTraverseOps

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.io.Source
import scala.util.Try

object DBService {
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
  private val dataPath = "/Users/guntissmaukstelis/sandbox/WeatherTool/data/"

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

  def save(fileName: String, content: String): IO[Unit] = {
    val path = Paths.get(s"$dataPath/$fileName")
    // TODO redeemWith instead of flatMap
    IO(Files.writeString(path, content)).attempt.flatMap {
      case Right(_) => IO.println(s"write: $fileName")
      case Left(error) => IO.println(s"Write file '$fileName' failed with error: ${error.getMessage}")
    }
  }

  // TODO remove this. Just testing
  private def run: IO[Unit] = {
    println("----------------> db main")

    val from = LocalDateTime.parse("20230414_2200", dateFormatter)
    val to = LocalDateTime.parse("20230501_1230", dateFormatter)

    for {
      lines <- getInRange(from, to)
      _ <- lines.traverse(IO.println)
    } yield ()
  }

  def main(args: Array[String]): Unit = {
    run.unsafeRunSync()
  }
}
