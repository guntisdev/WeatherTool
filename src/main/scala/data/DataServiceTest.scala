package data

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import data.DataService.deletionResultEncoder
import io.circe.syntax._

object DataServiceTest {
  def main(args: Array[String]): Unit = {
//    getForecasts().unsafeRunSync()
    deleteOldForecasts().unsafeRunSync()
  }

  private def deleteOldForecasts(): IO[Unit] = {
    val program = for {
      dataService <- DataService.of
      deleteList <- dataService.deleteOldForecasts()
      _ <- IO.println(deleteList.asJson)
    } yield ()

    program
  }

  private def getForecasts(): IO[Unit] = {
    val program = for {
      dataService <- DataService.of
      forecasts <- dataService.getForecasts()
      _ <- IO.println(forecasts.asJson)
    } yield ()

    program
  }
}
