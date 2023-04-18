package parse

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.io.Source
import scala.util.Try

/*
import cats.effect.{IO, Resource}
import java.io.File

def readFile(file: File): IO[String] =
  IO(scala.io.Source.fromFile(file).mkString).handleErrorWith(_ => IO.pure(""))

def readFiles(dir: File): IO[List[(String, String)]] =
  IO(dir.listFiles.toList)
    .flatMap(files =>
      files.traverse { file =>
        readFile(file).map((file.getName, _))
      }
    )
    .handleErrorWith(_ => IO.pure(List.empty))
 */

object Parser {
  val data_path = "/Users/guntissmaukstelis/sandbox/hello/data/"

  def fileToDateTime(file: File): LocalDateTime = {
    val dateString = file.toString.split("/").last.split("\\.").head
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    LocalDateTime.parse(dateString, formatter)
  }

  def getFiles(path: String): List[File] = new File(path).listFiles.toList

  def getFilesInRange(start: LocalDateTime, end: LocalDateTime): List[File] = {
    val fileMap = getFiles(data_path).map(file => fileToDateTime(file) -> file).toMap
    fileMap
      .filter { case (k, _) =>
        k.plusSeconds(1).isAfter(start) && k.minusSeconds(1).isBefore(end)
      }
      .map { case (_, v) => v }.toList
  }

  // TODO I guess this should be wrapped in IO
  def readFromFile(file: File): List[String] = {
    val source = Source.fromFile(file)
    val lineList = source.getLines.toList
    source.close()
    lineList.tail // remove header line
  }

  def parseLine(line: String): Option[WeatherStationData] = {
    val paramCount = MeteoData.getCount

    def parseTimestamp(timestampStr: String): Option[LocalDateTime] = {
      val formatter = DateTimeFormatter.ofPattern("yyyyMM.dd HH:mm")
      Try(LocalDateTime.parse(s"2023${timestampStr.trim}", formatter)).toEither match {
        case Right(timestamp) => Some(timestamp)
        case Left(_) => None
      }
    }

    def splitParts(parts: List[String]): Option[(String, String, List[String])] = {
      parts.splitAt(2) match {
        case (cityStr :: timestampStr :: Nil, rest) if rest.size >= paramCount => Some((cityStr, timestampStr, rest))
        case _ => None
      }
    }

    for {
      (city, timestampStr, rest) <- splitParts(line.trim.split(";", -1).toList)
      timestamp <- parseTimestamp(timestampStr)
      (strList, meteoPhenomenaList) = rest.splitAt(paramCount)
      meteoData <- MeteoData.fromDoubles(strList.map(_.toDoubleOption))
      phenomena <- Some(meteoPhenomenaList.map(_.trim))
    } yield WeatherStationData(city, timestamp, meteoData, phenomena)
  }

  def readFromVariable(str: String): List[String] = str.split("\n").toList // Data.csv

  def queryData(from: LocalDateTime, to: LocalDateTime, cities: List[String], aggregator: AggregateMeteo): Map[String, Double] = {
    val res = getFilesInRange(from, to)
      .flatMap(readFromFile)
      .flatMap(parseLine)
      .filter(line => cities.contains(line.city))
      .groupBy(_.city)

//    res.foreach(println)

    aggregator match {
      case AggregateMeteo.tempAvg => res.map { case (city, weatherData) => city -> weatherData.flatMap(_.meteo.tempMax).max }
      case AggregateMeteo.tempAvg => res.map { case (city, weatherData) => city -> weatherData.flatMap(_.meteo.tempMin).min }
      case AggregateMeteo.tempAvg => res.map { case (city, weatherData) => city -> {
        val avgList = weatherData.flatMap(_.meteo.tempAvg)
        avgList.sum / avgList.length
      }}
      case AggregateMeteo.precipitationSum => res.map { case (city, weatherData) => city -> weatherData.flatMap(_.meteo.precipitation).sum }
      //  TODO add here other aggregateParams
    }
  }

  def main(args: Array[String]): Unit = {
    println("================ start parser")

    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    val start = LocalDateTime.parse("20230409_2200", formatter)
    val end = LocalDateTime.parse("20230501_1230", formatter)

    val parsed = queryData(start, end, List("Liepāja", "Rēzekne", "randomstr"), AggregateMeteo.tempAvg)
//    parsed.foreach(println)
      println(parsed.toString())
  }
}