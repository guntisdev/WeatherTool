package server

import parse.Aggregate.AggregateKey

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
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
}
