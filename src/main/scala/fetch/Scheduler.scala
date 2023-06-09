package fetch

import cats.effect._
import cats.implicits.catsSyntaxApply
import fs2.Stream
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.{Duration, LocalTime}
import scala.concurrent.duration._


object Scheduler {
  def of: IO[Scheduler] = {
    Slf4jLogger.create[IO].map {
      new Scheduler(_)
    }
  }
}

class Scheduler(log: Logger[IO]) {
  private val downloadMinute = 31

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

  def scheduleTask(task: IO[Either[Throwable, (String, String)]]): Stream[IO, Either[Throwable, (String, String)]] = {
    Stream.eval(durationToNextHalfHour).flatMap { delay => {
      Stream.eval(log.info(s"Scheduler started with delay: ${delay.toMinutes} min")) *>
      (Stream.sleep[IO](delay) ++ Stream.awakeEvery[IO](1.hour))
        .evalMap(_ => task)
    }}
  }
}