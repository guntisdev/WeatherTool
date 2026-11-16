package fetch.dmi

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import data.DataService

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
      dataService <- DataService.of
      fetch <- FetchService.of(dataService)
      available <- fetch.fetchAvailableForecasts()
      nameList <- fetch.fetchFromList(available._2.take(2))
      _ <- IO.println(nameList)
    } yield ()

    program
  }
}
