package fetchDMI;

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import java.time.{ZoneOffset, ZonedDateTime}

object FetchServiceTest {
  def main(args: Array[String]): Unit = {
//    fetchFromTimeList().unsafeRunSync()
    fetchAvailableForecasts.unsafeRunSync()
  }

  private def fetchAvailableForecasts(): IO[Unit] = {
    val program = for {
      fetch <- FetchService.of
      result <- fetch.fetchAvailableForecasts()
      (modelRun, forecastTimes) = result
      _ <- IO.println(modelRun)
      _ <- IO.println(forecastTimes)
    } yield ()

    program
  }

  private def fetchFromTimeList(): IO[Unit] = {
    val program = for {
      nowUTC <- IO(ZonedDateTime.now(ZoneOffset.UTC))
      referenceTime = FileName.getClosestReferenceTime(nowUTC)
      timeList = FileName.generateTimeList(referenceTime)
      fetch <- FetchService.of
      nameList <- fetch.fetchFromList(timeList)
      _ <- IO.println(nameList)
    } yield ()

    program
  }
}
