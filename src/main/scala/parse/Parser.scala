package parse

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.io.Source
import scala.util.Try

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


  def main(args: Array[String]): Unit = {
    println("================ start parser")

    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    val start = LocalDateTime.parse("20230409_2200", formatter)
    val end = LocalDateTime.parse("20230501_1230", formatter)
    val files = getFilesInRange(start, end)
    val weatherStationData = files
      .flatMap(readFromFile)
      .flatMap(parseLine)

    //    weatherStationData.foreach(println)

    val liepajaWeather = weatherStationData.filter(_.city == "Liepāja")
    val maxTempLiepaja = liepajaWeather.flatMap(_.meteo.tempMax).max
    val minTempLiepaja = liepajaWeather.flatMap(_.meteo.tempMin).min
    val avgTemps = liepajaWeather.flatMap(_.meteo.tempAvg)
    val avgTempLiepaja = avgTemps.sum / avgTemps.size
    println(s"Liepaja max: $maxTempLiepaja, min: $minTempLiepaja, avg: $avgTempLiepaja")
    //    liepaja.foreach(println)

  }
}