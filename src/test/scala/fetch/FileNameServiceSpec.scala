import cats.Applicative
import cats.effect.unsafe.implicits.global
import org.scalatest.funsuite.AnyFunSuite
import cats.effect.{Clock, IO}
import fetch.csv.FileNameService

import java.util.concurrent.TimeUnit
import java.time.LocalDateTime
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._


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

    val actualFileNames = new FileNameService().generate(startTime, endTime)

    assert(actualFileNames == expectedFileNames)
  }

  test("FileNameService.generateFromDate returns correct file names") {
    val date = LocalDateTime.of(2023, 5, 17, 0, 0).toLocalDate

    val expectedFileNames = (0 to 23).toList
      .map(hour => if (hour < 10) "0" + hour else "" + hour)
      .map(str => s"20230517_${str}30.csv")

    val actualFileNames = new FileNameService().generateFromDate(date)

    assert(actualFileNames == expectedFileNames)
  }

  val fixedClock: Clock[IO] = new Clock[IO] {
    val timeInMillis = 1618862447000L

    override def realTime: IO[FiniteDuration] =
      IO.pure(Duration.fromNanos(TimeUnit.MILLISECONDS.toNanos(timeInMillis)))

    override def applicative: Applicative[IO] =
      Applicative.apply

    override def monotonic: IO[FiniteDuration] =
      IO.pure(Duration.fromNanos(TimeUnit.MILLISECONDS.toNanos(timeInMillis)))

    override def timed[A](fa: IO[A]): IO[(FiniteDuration, A)] =
      fa.map(a => (Duration.Zero, a))
  }

  test("FileNameService.generateLast24Hours returns correct file names for a fixed time") {
    val fileNameService = new FileNameService()
    val filenames = fileNameService.generateLast24Hours(fixedClock).unsafeRunSync()

    assert(filenames.size == 24)

    val expectedFileNames = (0 to 23).toList
      .map(hour => if (hour < 10) "0" + hour else "" + hour)
      .map(str => s"20210419_${str}30.csv")

    assert(filenames.sorted == expectedFileNames.sorted)
  }
}