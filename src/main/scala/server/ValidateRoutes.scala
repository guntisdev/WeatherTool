package server

import parse.Aggregate.AggregateKey

import java.time.{LocalDate, LocalDateTime}
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

  object ValidDate {
    def unapply(str: String): Option[LocalDate] = {
      val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
      Try(LocalDate.parse(str, formatter)).toOption
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
    def unapply(str: String): Option[List[String]] = {
      str.split(",").toList match {
        case Nil => None
        case list => Some(list)
      }
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
}
