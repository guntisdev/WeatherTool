package fetch

import cats.effect.{IO, Ref}
import cats.implicits.toTraverseOps
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.{LocalDate, LocalDateTime}

object StatefulFetchService {
  def of(fetchService: FetchService): IO[StatefulFetchService] = {
    Slf4jLogger.create[IO].map(logger => new StatefulFetchService(fetchService, new FileNameService(), logger))
  }
}

class StatefulFetchService(fetchService: FetchService, fileNameService: FileNameService, log: Logger[IO]) extends FetchServiceTrait {
  private val state: Ref[IO, Map[String, String]] = Ref.unsafe(Map.empty)

  private def logState: IO[Unit] = {
    state.get.flatMap(currentState => log.info(s"State keys: ${currentState.keys}"))
  }

  private def updateState(fileName: String, content: String): IO[Unit] = {
    for {
      last24Hours <- fileNameService.generateLast24Hours
      _ <- state.update(st => (st + (fileName -> content)).filterKeys(last24Hours.contains).toMap)
      _ <- logState
    } yield ()
  }

  def fetchSingleFile(fileName: String): IO[Either[Throwable, (String, String)]] = {
    fetchService.fetchSingleFile(fileName).flatMap {
      case Right((fileName, content)) =>
        updateState(fileName, content).as(Right((fileName, content)))
      case e@Left(_) => IO(e)
    }
  }

  def fetchInRange(from: LocalDateTime, to: LocalDateTime): IO[List[Either[Throwable, (String, String)]]] = {
    fetchService.fetchInRange(from, to).flatMap { results =>
      val successfulResults = results.collect { case Right(data) => data }
      successfulResults.traverse { case (name, content) => updateState(name, content) }.as(results)
    }
  }

  def fetchFromDate(date: LocalDate): IO[List[Either[Throwable, (String, String)]]] = {
    fetchService.fetchFromDate(date).flatMap { results =>
      val successfulResults = results.collect { case Right(data) => data }
      successfulResults.traverse { case (name, content) => updateState(name, content) }.as(results)
    }
  }
}







