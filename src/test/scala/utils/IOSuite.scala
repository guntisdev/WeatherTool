package base

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import org.scalatest.Succeeded

import scala.concurrent.Future
import scala.concurrent.duration._

trait IOSuite {

  val Timeout: FiniteDuration = 5.seconds

  implicit val ioRuntime: IORuntime = IORuntime.global

  def runIO[A](io: IO[A], timeout: FiniteDuration = Timeout): Future[Succeeded.type] =
    io.timeout(timeout).as(Succeeded).unsafeToFuture()
}