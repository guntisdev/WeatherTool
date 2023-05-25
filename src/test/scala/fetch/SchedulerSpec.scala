package fetch

import cats.effect.{Clock, IO}
import fs2.Stream

class SchedulerSpec {
//    private def testTask: IO[Unit] = {
//      log.info("Running task")
//    }
//
//  def run(implicit clock: Clock[IO]): Stream[IO, Nothing] = {
//    Stream.eval(durationToNextHalfHour).flatMap { delay =>
//      (Stream.sleep[IO](delay) ++ Stream.awakeEvery[IO](1.hour)).evalMap(_ => testTask).drain
//    }
//  }
//
//  def main(args: Array[String]): Unit = {
//    //    run.compile.drain.unsafeRunSync()
//    for {
//      scheduler <- Scheduler.of
//      fetch <- FetchService.of
//      fetchTask = new FileNameService().generateCurrentHour.flatMap(fetch.fetchSingleFile)
//    } yield scheduler.scheduleTask(fetchTask).compile.drain.unsafeRunSync()
//  }
}
