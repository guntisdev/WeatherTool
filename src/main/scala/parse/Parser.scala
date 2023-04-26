package parse

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object Parser {
  def parseLine(line: String): Option[WeatherStationData] = {
    val paramCount = MeteoData.getCount

    def parseTimestamp(timestampStr: String): Option[LocalDateTime] = {
      val formatter = DateTimeFormatter.ofPattern("yyyydd.MM HH:mm")
      // TODO figure out what to do with hardcoded year. Proly fetched data should be also modified to inlcude year
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

  def queryData(
    lines: List[String],
    cities: List[String],
    aggregator: AggregateKey,
  ): Map[String, AggregateValue] = {
    val weatherByCity = lines
      .flatMap(parseLine)
      .filter(line => cities.contains(line.city))
      .groupBy(_.city)

    def aggregateValue(
      selector: MeteoData => Option[Double],
      aggregate: List[Double] => Double,
      constructor: Double => AggregateValue
    ): Map[String, AggregateValue] = {
      weatherByCity.map { case (city, weatherData) =>
        val values = weatherData.flatMap(wd => selector(wd.meteo))
        city -> constructor(aggregate(values))
      }
    }

    def aggregateList(
      selector: WeatherStationData => Option[(LocalDateTime, Double)],
      constructor: Map[LocalDateTime, Double] => AggregateValue
    ): Map[String, AggregateValue] = {
      weatherByCity.map { case (city, weatherData) =>
        val values = weatherData.flatMap(selector).toMap
        city -> constructor(values)
      }
    }

    val AggKey = AggregateKey
    val AggVal = AggregateValue

    aggregator match {
      case AggKey.tempMax => aggregateValue(_.tempMax, _.max, AggVal.tempMax)
      case AggKey.tempMaxList => aggregateList(wd => wd.meteo.tempMax.map(wd.timestamp -> _), AggVal.tempMaxList)

      case AggKey.tempMin => aggregateValue(_.tempMin, _.min, AggVal.tempMin)
      case AggKey.tempMinList => aggregateList(wd => wd.meteo.tempMin.map(wd.timestamp -> _), AggVal.tempMinList)

      case AggKey.tempAvg => aggregateValue(_.tempAvg, values => values.sum / values.length, AggVal.tempAvg)
      case AggKey.tempAvgList => aggregateList(wd => wd.meteo.tempAvg.map(wd.timestamp -> _), AggVal.tempAvgList)

      case AggKey.precipitationSum => aggregateValue(_.precipitation, _.sum, AggVal.precipitationSum)
      case AggKey.precipitationList => aggregateList(wd => wd.meteo.precipitation.map(wd.timestamp -> _), AggVal.precipitationList)
    }
  }
}