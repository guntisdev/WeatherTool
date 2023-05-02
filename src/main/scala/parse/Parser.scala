package parse

import parse.Aggregate.{AggregateValue, UserQuery, aggregateDoubleValues, aggregatePhenomenaValues, extractDoubleFieldValues}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object Parser {
  def parseLine(line: String): Option[WeatherStationData] = {
    val paramCount = WeatherData.getDoubleParamCount

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
      (strList, phenomenaList) = rest.splitAt(paramCount)
      phenomena <- Some(phenomenaList.map(_.trim))
      weatherData <- WeatherData.fromDoubles(strList.map(_.toDoubleOption), phenomena)
    } yield WeatherStationData(city, timestamp, weatherData)
  }

  def queryData(userQuery: UserQuery, lines: List[String]): Map[String, Option[AggregateValue]] = {
    val weatherByCity = lines
      .flatMap(parseLine)
      .filter(line => userQuery.cities.contains(line.city))
      .groupBy(_.city)

    weatherByCity.map { case (city, weatherStationData) =>
      userQuery.field match {
        case "phenomena" => {
          val phenomenaList = weatherStationData.map(_.weather.phenomena)
          (city -> aggregatePhenomenaValues(userQuery.key, phenomenaList))
        }
        case field => {
          val doubleList = extractDoubleFieldValues(field, weatherStationData.map(_.weather))
          (city -> aggregateDoubleValues(userQuery.key, doubleList))

        }
      }
    }
  }
}