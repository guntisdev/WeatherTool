package data

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.syntax.EncoderOps

object DataServiceTest {
  def main(args: Array[String]): Unit = {
    val program = for {
        forecasts <- DataService.getForecasts()
        _ <- IO.println(forecasts.asJson)
    } yield ()

    program.unsafeRunSync()
  }
}
