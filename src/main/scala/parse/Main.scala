package parse

import cats.effect.unsafe.implicits.global
import db.FileService
import io.circe.syntax.EncoderOps
import parse.Aggregate.{AggregateKey, UserQuery}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import parse.Aggregate.AggregateValueImplicits.aggregateValueEncoder

// TODO remake this as a test with different granularities and especially check avg value calculations
object Main {
  def main(args: Array[String]): Unit = {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")

    def groupByGranularity(granularity: ChronoUnit) = {
      granularity match {
        case ChronoUnit.DAYS => (dateTime: LocalDateTime) => dateTime.toLocalDate
        case ChronoUnit.MONTHS => (dateTime: LocalDateTime) => dateTime.toLocalDate.withDayOfMonth(1)
        case ChronoUnit.YEARS => (dateTime: LocalDateTime) => dateTime.toLocalDate.withDayOfYear(1)
        case _ => (dateTime: LocalDateTime) => dateTime.withMinute(0).withSecond(0).withNano(0)
      }
    }

    val list = List(
      (LocalDateTime.parse("20230515_0900", formatter), Some(20d)),
      (LocalDateTime.parse("20230515_1000", formatter), None),
      (LocalDateTime.parse("20230515_1100", formatter), Some(6d)),
      (LocalDateTime.parse("20230516_1100", formatter), Some(10d)),
      (LocalDateTime.parse("20230416_1100", formatter), Some(10d)),
    )

    val grouped = list.groupBy{ case (dateTime, _) => groupByGranularity(ChronoUnit.HOURS)(dateTime) }

//    println(grouped)

//    grouped.foreach { case (key, value) =>
//      println(s"$key -> $value")
//    }

    val from = LocalDateTime.parse("20230627_0000", formatter)
    val to = LocalDateTime.parse("20230627_2359", formatter)

    val fileService = FileService.of.unsafeRunSync()
    val lines = fileService.getInRange(from, to).unsafeRunSync()
    val query = UserQuery(List("Ainaži"), "tempAvg", AggregateKey.Avg, ChronoUnit.HOURS)
    val parsed = Parser.queryData(query, lines)
    println(parsed.asJson)


//    val query2 = UserQuery(List("Ainaži"), "tempAvg", AggregateKey.Avg, ChronoUnit.DAYS)
//    val parsed2 = Parser.queryData(query2, lines)
//    println(parsed2.asJson)


//    val query3 = UserQuery(List("Ainaži"), "tempAvg", AggregateKey.Avg, ChronoUnit.MONTHS)
//    val parsed3 = Parser.queryData(query3, lines)
//    println(parsed3.asJson)

  }
}
