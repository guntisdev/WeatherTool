package parse

import cats.effect.unsafe.implicits.global
import db.DBService
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import parse.Aggregate.{AggregateKey, DoubleValue, TimeDoubleList, UserQuery}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.collection.immutable.HashMap

class ParserSpec extends AnyFunSuite with Matchers {

  test("QueryData should return correct sum result") {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    val from = LocalDateTime.parse("20230515_0905", formatter)
    val to = LocalDateTime.parse("20230516_0942", formatter)
    val userQuery = UserQuery(List("Bauska", "Dagda", "Daugavgrīva", "Rīga"), "precipitation", AggregateKey.Sum)

    val dbService = DBService.of.unsafeRunSync()

    val lines = dbService.getInRange(from, to).unsafeRunSync()
    val parsed = Parser.queryData(userQuery, lines)

    parsed shouldBe HashMap(
      "Dagda" -> Some(DoubleValue(0.6)),
      "Rīga" -> Some(DoubleValue(7.9)),
      "Daugavgrīva" -> Some(DoubleValue(5.9)),
      "Bauska" -> Some(DoubleValue(3.0))
    )
  }

  test("QueryData should return correct list result") {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
    val from = LocalDateTime.parse("20230516_0400", formatter)
    val to = LocalDateTime.parse("20230516_0800", formatter)
    val userQuery = UserQuery(List("Rīga"), "precipitation", AggregateKey.List)

    val dbService = DBService.of.unsafeRunSync()

    val lines = dbService.getInRange(from, to).unsafeRunSync()
    val parsed = Parser.queryData(userQuery, lines)

    parsed shouldBe HashMap(
      "Rīga" ->
        Some(TimeDoubleList(List(
          (LocalDateTime.parse("2023-05-16T04:00"), Some(1.9)),
          (LocalDateTime.parse("2023-05-16T05:00"), Some(4.5)),
          (LocalDateTime.parse("2023-05-16T06:00"), Some(1.5)),
          (LocalDateTime.parse("2023-05-16T07:00"), Some(0.0)),
        )))
    )
  }
}