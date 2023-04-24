package parse

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

  // TODO move out
  def queryData(
     data: List[String],
     cities: List[String],
     aggregator: AggregateMeteo,
   ): Map[String, Double] = {
    aggregateLines(data, cities, aggregator)
  }
}