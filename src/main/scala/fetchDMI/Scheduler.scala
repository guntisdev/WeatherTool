package fetchDMI

import cats.effect._
import cats.implicits.catsSyntaxApply
import fs2.Stream
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.{Duration, LocalTime}
import scala.concurrent.duration._


object Scheduler {
  def of(name: String, minutes: List[Int]): IO[Scheduler] = {
    Slf4jLogger.create[IO].map(logger => new Scheduler(name, minutes, logger))
  }
}

class Scheduler(name: String, minutes: List[Int], log: Logger[IO]) {
  private def durationToNext(targetMinute: Int)(implicit clock: Clock[IO]): IO[FiniteDuration] = {
    clock.realTime.map { duration =>
      val now = LocalTime.ofSecondOfDay((duration.toMillis / 1000) % (24 * 60 * 60))
      val nextTime =
        if (now.getMinute < targetMinute) now.withMinute(targetMinute)
        else now.plusHours(1).withMinute(targetMinute)
      val durationToNext = Duration.between(now, nextTime)
      FiniteDuration(durationToNext.toMillis, MILLISECONDS)
    }
  }

  def scheduleTask(task: IO[List[String]]): Stream[IO, List[String]] = {
    def streamForMinute(minute: Int): Stream[IO, List[String]] = {
      Stream.eval(durationToNext(minute)).flatMap { delay =>
      Stream.eval(log.info(s"$name scheduler in: ${delay.toMinutes} min")) *>
        (Stream.sleep[IO](delay) ++ Stream.awakeEvery[IO](1.hour))
          .evalMap(_ => task)
      }
    }

    minutes.map(streamForMinute).reduce(_ merge _)
  }
}