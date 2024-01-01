package parse

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object Parser {
  def parseLine(line: String, year: String): Option[WeatherStationData] = {
    val paramCount = WeatherData.getDoubleParamCount

    def parseTimestamp(timestampStr: String): Option[LocalDateTime] = {
      val formatter = DateTimeFormatter.ofPattern("yyyydd.MM HH:mm")
      Try(LocalDateTime.parse(s"$year${timestampStr.trim}", formatter)).toEither match {
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
}