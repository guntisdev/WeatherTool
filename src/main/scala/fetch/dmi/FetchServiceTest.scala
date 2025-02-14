package fetch.dmi

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import data.DataService

import java.time.{ZoneOffset, ZonedDateTime}

object FetchServiceTest {
  def main(args: Array[String]): Unit = {
//    fetchFromTimeList().unsafeRunSync()
//    fetchAvailableForecasts.unsafeRunSync()
//    generateFetchList().unsafeRunSync()
    fetchRecentForecasts().unsafeRunSync()
  }

  private def fetchRecentForecasts(): IO[Unit] = {
    val program = for {
      dataService <- DataService.of
      fetch <- FetchService.of(dataService)
      result <- fetch.fetchRecentForecasts()
      _ <- IO.println("-=fetch finished=-")
    } yield ()
    program
  }

  private def generateFetchList(): IO[Unit] = {
    val program = for {
      dataService <- DataService.of
      fetch <- FetchService.of(dataService)
      list <- fetch.generateFetchList()
      _ <- IO.println(list)
    } yield ()
    program
  }

  private def fetchAvailableForecasts(): IO[Unit] = {
    val program = for {
      dataService <- DataService.of
      fetch <- FetchService.of(dataService)
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
      dataService <- DataService.of
      fetch <- FetchService.of(dataService)
      nameList <- fetch.fetchFromList(timeList)
      _ <- IO.println(nameList)
    } yield ()

    program
  }
}
