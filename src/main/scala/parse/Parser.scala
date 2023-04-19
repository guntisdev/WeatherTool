package parse

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object Parser {
  def parseLine(line: String): Option[WeatherStationData] = {
    val paramCount = MeteoData.getCount

    def parseTimestamp(timestampStr: String): Option[LocalDateTime] = {
      val formatter = DateTimeFormatter.ofPattern("yyyydd.MM HH:mm")
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

  private def aggregateLines(
    lines: List[String],
    cities: List[String],
    aggregator: AggregateMeteo,
  ): Map[String, Double] = {
    val weatherByCity = lines
      .flatMap(parseLine)
      .filter(line => cities.contains(line.city))
      .groupBy(_.city)

    aggregator match {
      case AggregateMeteo.tempAvg => weatherByCity.map { case (city, weatherData) => city -> weatherData.flatMap(_.meteo.tempMax).max }
      case AggregateMeteo.tempAvg => weatherByCity.map { case (city, weatherData) => city -> weatherData.flatMap(_.meteo.tempMin).min }
      case AggregateMeteo.tempAvg => weatherByCity.map { case (city, weatherData) => city -> {
        val avgList = weatherData.flatMap(_.meteo.tempAvg)
        avgList.sum / avgList.length
      }
      }
      case AggregateMeteo.precipitationSum => weatherByCity.map { case (city, weatherData) => city -> weatherData.flatMap(_.meteo.precipitation).sum }
      //  TODO add here other aggregateParams
    }
  }

  def queryData(
     from: LocalDateTime,
     to: LocalDateTime,
     cities: List[String],
     aggregator: AggregateMeteo,
   ): IO[Map[String, Double]] = {
    for {
      lines <- db.DBService.getInRange(from, to)
      weatherLines <- IO.pure(aggregateLines(lines, cities, aggregator))
//      _ <- IO.println(weatherLines)
    } yield weatherLines
  }

  private def run: IO[Unit] = {
    println("================ start parser")

    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    val start = LocalDateTime.parse("20230409_2200", formatter)
    val end = LocalDateTime.parse("20230501_1230", formatter)

    for {
      parsed <- queryData(start, end, List("Liepāja", "Rēzekne", "randomstr"), AggregateMeteo.tempAvg)
      _ <- IO.println(parsed)
    } yield ()
  }

  def main(args: Array[String]): Unit = {
    run.unsafeRunSync()
  }
}