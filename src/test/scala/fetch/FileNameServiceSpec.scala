import fetch.FileNameService
import org.scalatest.funsuite.AnyFunSuite

import java.time.LocalDateTime

class FileNameServiceSpec extends AnyFunSuite {

  test("FileNameService.generate returns correct file names") {
    val startTime = LocalDateTime.of(2023, 5, 17, 0, 0)
    val endTime = LocalDateTime.of(2023, 5, 17, 4, 0)

    val expectedFileNames = List(
      "20230517_0030.csv",
      "20230517_0130.csv",
      "20230517_0230.csv",
      "20230517_0330.csv",
    )

    val actualFileNames = FileNameService.generate(startTime, endTime)

    assert(actualFileNames == expectedFileNames)
  }

  test("FileNameService.generateFromDate returns correct file names") {
    val date = LocalDateTime.of(2023, 5, 17, 0, 0).toLocalDate

    val expectedFileNames = (0 to 23).toList
      .map(hour => if(hour < 10) "0"+hour else ""+hour)
      .map(str => s"20230517_${str}30.csv")

    val actualFileNames = FileNameService.generateFromDate(date)

    assert(actualFileNames == expectedFileNames)
  }
}