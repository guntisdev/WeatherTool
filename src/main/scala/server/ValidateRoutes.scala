package server

import cats.data.NonEmptyList
import parse.csv.Aggregate.AggregateKey
import parse.csv.WeatherData

import java.time.{LocalDate, LocalDateTime, ZonedDateTime}
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import scala.util.Try

object ValidateRoutes {
  object DateTimeRange {
    def unapply(str: String): Option[(LocalDateTime, LocalDateTime)] = {
      val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
      str.split("-")
        .toList
        .map(str => Try(LocalDateTime.parse(str, formatter)).toOption) match {
        case List(Some(from), Some(to)) => Some(from, to)
        case _ => None
      }
    }
  }

  object ValidateDate {
    def unapply(str: String): Option[LocalDate] = {
      val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
      Try(LocalDate.parse(str, formatter)).toOption
    }
  }

  object ValidateDateTime {
    def unapply(str: String): Option[LocalDateTime] = {
      val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
      Try(LocalDateTime.parse(str, formatter)).toOption
    }
  }

  object ValidateZonedDateTime {
    def unapply(str: String): Option[ZonedDateTime] = {
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddTHH:mm:ssZ")
      Try(ZonedDateTime.parse(str, formatter)).toOption
    }
  }

  object ValidateMonths {
    def unapply(str: String): Option[List[LocalDate]] = {
      val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
      val monthList = str
        .split(",")
        .toList
        .flatMap(str => Try(LocalDate.parse(str + "01", formatter)).toOption)

      monthList match {
        case Nil => None
        case list => Some(list)
      }
    }
  }

  object CityList {
    def unapply(str: String): Option[NonEmptyList[String]] = {
      NonEmptyList.fromList(str.split(",").toList)
    }
  }

  object AggFieldList {
    def unapply(str: String): Option[NonEmptyList[String]] = {
      val weatherFields = WeatherData.getKeys
      val filteredList = str.split(",").toList.filter(weatherFields.contains)
      NonEmptyList.fromList(filteredList)
    }
  }

  object AggKey {
    def unapply(str: String): Option[AggregateKey] = {
      AggregateKey.fromString(str)
    }
  }

  object Granularity {
    def unapply(str: String): Option[ChronoUnit] = {
      str match {
        case "hour" => Some(ChronoUnit.HOURS)
        case "day" => Some(ChronoUnit.DAYS)
        case "month" => Some(ChronoUnit.MONTHS)
        case "year" => Some(ChronoUnit.YEARS)
        case _ => None
      }
    }
  }

  object ValidateInt {
    def unapply(str: String): Option[Int] = {
      Option(str.toInt)
    }
  }
}
