package server

import cats.effect._
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxApply
import fetch.{FetchService, FileNameService}
import fs2.Stream

import java.time.{Duration, LocalTime}
import scala.concurrent.duration._

object Scheduler {
  private val downloadMinute = 31

//  private def testTask: IO[Unit] = {
//    IO(println("Running task"))
//  }

  def durationToNextHalfHour(implicit clock: Clock[IO]): IO[FiniteDuration] = {
    clock.realTime.map { duration =>
      val now = LocalTime.ofSecondOfDay((duration.toMillis / 1000) % (24 * 60 * 60))
      val nextHalfHour = if (now.getMinute < downloadMinute) now.withMinute(downloadMinute)
      else now.plusHours(1).withMinute(downloadMinute)
      val durationToNext = Duration.between(now, nextHalfHour)
      FiniteDuration(durationToNext.toMillis, MILLISECONDS)
//      FiniteDuration(2000, MILLISECONDS)
    }
  }

//  def run(implicit clock: Clock[IO]): Stream[IO, Nothing] = {
//    Stream.eval(durationToNextHalfHour).flatMap { delay =>
//      (Stream.sleep[IO](delay) ++ Stream.awakeEvery[IO](1.hour)).evalMap(_ => testTask).drain
//    }
//  }

  def scheduleTask(task: IO[(String, String)]): Stream[IO, (String, String)] = {
    Stream.eval(durationToNextHalfHour).flatMap { delay => {
      Stream.eval(IO.println(s"Scheduler started with delay: ${delay.toMinutes} min")) *>
      (Stream.sleep[IO](delay) ++ Stream.awakeEvery[IO](1.hour))
        .evalMap(_ => task)
    }}
  }

  def main(args: Array[String]): Unit = {
//    run.compile.drain.unsafeRunSync()

    val fetchTask = FileNameService.generateCurrentHour.flatMap(FetchService.fetchSingleFile)
    scheduleTask(fetchTask).compile.drain.unsafeRunSync()
  }
}
