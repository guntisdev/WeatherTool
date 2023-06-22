package db

import cats.effect.unsafe.implicits.global
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.io.IOException
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}


class DBServiceSpec extends AnyFunSuite with Matchers {
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")

  test("getDateFileNames should return correct file names") {
    val dbService = DBService.of.unsafeRunSync()
    val datesList = dbService.getDateFileNames(LocalDate.of(2023, 5, 15))
      .unsafeRunSync()

    val expectedFileNames = (0 to 23).toList
      .map(hour => if (hour < 10) "0" + hour else "" + hour)
      .map(str => s"20230515_${str}30.csv")

    datesList shouldBe expectedFileNames
  }

  test("getInRange should return correct count of lines") {
    val from = LocalDateTime.parse("20230513_2200", dateFormatter)
    val to = LocalDateTime.parse("20230516_1230", dateFormatter)

    val dbService = DBService.of.unsafeRunSync()
    val lines = dbService.getInRange(from, to).unsafeRunSync()

    lines.length shouldBe 2142
  }

  test("DBService.save should return correct result") {
    val dbService = DBService.of.unsafeRunSync()
    val fileName = "testFile.txt"
    val fileContent = "test content..."

    dbService.save(fileName, fileContent).unsafeRunSync() shouldEqual fileName
  }

  test("DBService.save returns error on invalid file name") {
    val dbService = DBService.of.unsafeRunSync()
    val fileName = "/invalid/file/name"
    val fileContent = "test content..."

    val result = dbService.save(fileName, fileContent).attempt.unsafeRunSync()

    result match {
      case Left(e) =>
        assert(e.isInstanceOf[IOException])
      case Right(_) => fail("Expected failure did not occur")
    }
  }
}