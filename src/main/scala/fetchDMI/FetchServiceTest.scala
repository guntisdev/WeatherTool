package fetchDMI;

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import java.time.{ZoneOffset, ZonedDateTime}

object FetchServiceTest {
  def main(args: Array[String]): Unit = {
    val program = for {
      nowUTC <- IO(ZonedDateTime.now(ZoneOffset.UTC))
      referenceTime = FileName.getClosestReferenceTime(nowUTC)
      fetch <- FetchService.of
      _ <- fetch.fetchFromDateTime(referenceTime)
    } yield ()

    program.unsafeRunSync()
  }
}
