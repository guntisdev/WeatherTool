package db

import cats.effect.unsafe.implicits.global
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.io.IOException
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}


class FileServiceSpec extends AnyFunSuite with Matchers {
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")

  test("getDateFileNames should return correct file names") {
    val fileService = FileService.of.unsafeRunSync()
    val datesList = fileService.getDateFileNames(LocalDate.of(2023, 5, 15))
      .unsafeRunSync()

    val expectedFileNames = (0 to 23).toList
      .map(hour => if (hour < 10) "0" + hour else "" + hour)
      .map(str => s"20230515_${str}30.csv")

    datesList shouldBe expectedFileNames
  }

  test("getInRange should return correct count of lines") {
    val from = LocalDateTime.parse("20230513_2200", dateFormatter)
    val to = LocalDateTime.parse("20230516_1230", dateFormatter)

    val fileService = FileService.of.unsafeRunSync()
    val lines = fileService.getInRange(from, to).unsafeRunSync()

    lines.length shouldBe 2142
  }

  test("getDatesByMonths should return filtered dates") {
    val monthFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val monthList = List(
      LocalDate.parse("20230401", monthFormatter),
      LocalDate.parse("20230501", monthFormatter),
      LocalDate.parse("20230601", monthFormatter),
    )
    val fileService = FileService.of.unsafeRunSync()
    val dates = fileService.getDatesByMonths(monthList).unsafeRunSync()

    dates should not be empty

    val exportFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val validPrefixes = Set("2023-04", "2023-05", "2023-06")
    val invalidDates = dates
      .map(_.format(exportFormatter))
      .filterNot(date => validPrefixes.exists(prefix => date.startsWith(prefix)))

    invalidDates shouldBe empty
  }

  test("DBService.save should return correct result") {
    val fileService = FileService.of.unsafeRunSync()
    val fileName = "testFile.txt"
    val fileContent = "test content..."

    fileService.save(fileName, fileContent).unsafeRunSync() shouldEqual fileName
  }

  test("DBService.save returns error on invalid file name") {
    val fileService = FileService.of.unsafeRunSync()
    val fileName = "/invalid/file/name"
    val fileContent = "test content..."

    val result = fileService.save(fileName, fileContent).attempt.unsafeRunSync()

    result match {
      case Left(e) =>
        assert(e.isInstanceOf[IOException])
      case Right(_) => fail("Expected failure did not occur")
    }
  }
}