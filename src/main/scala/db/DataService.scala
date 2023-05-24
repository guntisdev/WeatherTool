package db

import cats.effect.{Clock, IO, Ref}
import cats.implicits.toTraverseOps
import fetch.FileNameService
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}

trait DataServiceTrait {
  def save(fileName: String, content: String): IO[Either[Throwable, String]]
  def readFile(fileName: String): IO[List[String]]
  def getInRange(from: LocalDateTime, to: LocalDateTime): IO[List[String]]
  def getDates: IO[List[LocalDate]]
  def getDateFileNames(date: LocalDate): IO[List[String]]
}

object DataService {
  def of(dbService: DBService): IO[DataService] = {
    for {
      log <- Slf4jLogger.create[IO]
      fileNameService = new FileNameService()
      fileNames <- fileNameService.generateLast24Hours
      contents <- fileNames.traverse(fileName => dbService.readFile(fileName)
        .map(content => (fileName, content)))
      state = contents.toMap
      stateRef <- Ref.of[IO, Map[String, List[String]]](state)
    } yield new DataService(dbService, new FileNameService(), log, stateRef)
  }
}
class DataService private(
                           dbService: DBService,
                           fileNameService: FileNameService,
                           log: Logger[IO],
                           private val state: Ref[IO, Map[String, List[String]]]
) extends DataServiceTrait {

  private def logState: IO[Unit] = {
    state.get.flatMap(currentState => log.info(s"State keys: ${currentState.keys.size}"))
  }

  private def filterState: IO[Unit] = {
    fileNameService.generateLast24Hours.flatMap { last24Hours =>
      state.update(st => st.filterKeys(last24Hours.contains).toMap)
    }
  }

  def save(fileName: String, content: String): IO[Either[Throwable, String]] = {
    dbService.save(fileName, content).flatMap {
      case Right(savedFileName) =>
        state.update(st => st.updated(savedFileName, content.split("\n").toList)) *>
          filterState *>
          logState.as(Right(savedFileName))
      case e@Left(_) => IO.pure(e)
    }
  }

  def readFile(fileName: String): IO[List[String]] = dbService.readFile(fileName)

  def getInRange(from: LocalDateTime, to: LocalDateTime): IO[List[String]] = dbService.getInRange(from, to)

  def getDates: IO[List[LocalDate]] = dbService.getDates

  def getDateFileNames(date: LocalDate): IO[List[String]] = dbService.getDateFileNames(date)

  // TODO implement getting full data from state
  def getLast24Hours: IO[List[String]] = {
    state.get.map(_.keys.toList.sorted)
  }
}