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

      case AggKey.windAvg => aggregateValue(_.windSpeedAvg, values => values.sum / values.length, AggVal.windAvg)
      case AggKey.windAvgList => aggregateList(wd => wd.meteo.windSpeedAvg.map(wd.timestamp -> _), AggVal.windAvgList)

      case AggKey.windMax => aggregateValue(_.windGustMax, _.max, AggVal.windMax)
      case AggKey.windMaxList => aggregateList(wd => wd.meteo.windGustMax.map(wd.timestamp -> _), AggVal.windMaxList)

      case AggKey.visibilityMin => aggregateValue(_.visibilityMin, _.min, AggVal.visibilityMin)
      case AggKey.visibilityMinList => aggregateList(wd => wd.meteo.visibilityMin.map(wd.timestamp -> _), AggVal.visibilityMinList)

      case AggKey.visibilityAvg => aggregateValue(_.visibilityAvg, values => values.sum / values.length, AggVal.visibilityAvg)
      case AggKey.visibilityAvgList => aggregateList(wd => wd.meteo.visibilityAvg.map(wd.timestamp -> _), AggVal.visibilityAvgList)

      case AggKey.snowAvgList => aggregateList(wd => wd.meteo.snowThicknessAvg.map(wd.timestamp -> _), AggVal.snowAvgList)
      case AggKey.snowAvg => aggregateValue(_.snowThicknessAvg, values => values.sum / values.length, AggVal.snowAvg)
      case AggKey.snowMax => aggregateValue(_.snowThicknessAvg, _.max, AggVal.snowMax)

      case AggKey.atmPressureList => aggregateList(wd => wd.meteo.atmPressure.map(wd.timestamp -> _), AggVal.atmPressureList)
      case AggKey.atmPressureMin => aggregateValue(_.atmPressure, _.min, AggVal.atmPressureMin)
      case AggKey.atmPressureMax => aggregateValue(_.atmPressure, _.max, AggVal.atmPressureMax)
      case AggKey.atmPressureAvg => aggregateValue(_.atmPressure, values => values.sum / values.length, AggVal.atmPressureAvg)

      case AggKey.dewList => aggregateList(wd => wd.meteo.dewPoint.map(wd.timestamp -> _), AggVal.dewList)
      case AggKey.dewMin => aggregateValue(_.dewPoint, _.min, AggVal.dewMin)
      case AggKey.dewMax => aggregateValue(_.dewPoint, _.max, AggVal.dewMax)
      case AggKey.dewAvg => aggregateValue(_.dewPoint, values => values.sum / values.length, AggVal.dewAvg)

      case AggKey.humidityAvg => aggregateValue(_.airHumidity, values => values.sum / values.length, AggVal.humidityAvg)
      case AggKey.humidityAvgList => aggregateList(wd => wd.meteo.airHumidity.map(wd.timestamp -> _), AggVal.humidityAvgList)

      case AggKey.sunDurationList => aggregateList(wd => wd.meteo.sunshineDuration.map(wd.timestamp -> _), AggVal.sunDurationList)
      case AggKey.sunDurationSum => aggregateValue(_.sunshineDuration, _.sum, AggVal.sunDurationSum)
    }
  }
}