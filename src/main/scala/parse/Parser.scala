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

  private def aggregateLines(
    lines: List[String],
    cities: List[String],
    aggregator: AggregateKey,
  ): Map[String, AggregateValue] = {
    val weatherByCity = lines
      .flatMap(parseLine)
      .filter(line => cities.contains(line.city))
      .groupBy(_.city)

    aggregator match {
      case AggregateKey.tempMax => weatherByCity.map { case (city, weatherData) => city ->
        AggregateValue.tempMax(weatherData.flatMap(_.meteo.tempMax).max) }
      case AggregateKey.tempMaxList => weatherByCity.map { case (city, weatherData) => city ->
        AggregateValue.tempMaxList(weatherData.collect {
          case wd if wd.meteo.tempMax.isDefined => wd.timestamp -> wd.meteo.tempMax.get
        }.toMap) }

      case AggregateKey.tempMin => weatherByCity.map { case (city, weatherData) => city ->
        AggregateValue.tempMin(weatherData.flatMap(_.meteo.tempMin).min) }
      case AggregateKey.tempMinList => weatherByCity.map { case (city, weatherData) => city ->
        AggregateValue.tempMinList(weatherData.collect {
          case wd if wd.meteo.tempMin.isDefined => wd.timestamp -> wd.meteo.tempMin.get
        }.toMap)}

      case AggregateKey.tempAvg => weatherByCity.map { case (city, weatherData) => city ->
        AggregateValue.tempAvg({
          val avgList = weatherData.flatMap(_.meteo.tempAvg)
          avgList.sum / avgList.length
        })}
      case AggregateKey.tempAvgList => weatherByCity.map { case (city, weatherData) => city ->
        AggregateValue.tempAvgList(weatherData.collect {
          case wd if wd.meteo.tempAvg.isDefined => wd.timestamp -> wd.meteo.tempAvg.get
        }.toMap)}

      case AggregateKey.precipitationSum => weatherByCity.map { case (city, weatherData) => city ->
        AggregateValue.precipitationSum(weatherData.flatMap(_.meteo.precipitation).sum)}
      case AggregateKey.precipitationList => weatherByCity.map { case (city, weatherData) => city ->
        AggregateValue.precipitationList(weatherData.collect {
          case wd if wd.meteo.precipitation.isDefined => wd.timestamp -> wd.meteo.precipitation.get
        }.toMap)
      }
      //  TODO add here other aggregateParams
    }
  }

  // TODO move out
  def queryData(
     data: List[String],
     cities: List[String],
     aggregator: AggregateKey,
   ): Map[String, AggregateValue] = {
    aggregateLines(data, cities, aggregator)
  }
}