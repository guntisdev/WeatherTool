package db

import cats.effect.unsafe.implicits.global
import cats.effect.{Clock, IO, Ref}
import cats.implicits.toTraverseOps
import fetch.FileNameService
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import parse.Aggregate
import parse.Aggregate.UserQuery

import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}

trait DataServiceTrait {
  def save(fileName: String, content: String): IO[String]
  def readFile(fileName: String): IO[List[String]]
  def getInRange(from: LocalDateTime, to: LocalDateTime): IO[List[String]]
  def getDates: IO[List[LocalDate]]
  def getDateFileNames(date: LocalDate): IO[List[String]]

  def getDatesByMonths(monthList: List[LocalDate]): IO[List[LocalDate]]
}

object DataService {
  def of(
    fileService: FileService,
    postgresService: PostgresService
  ): IO[DataService] = {
    for {
      log <- Slf4jLogger.create[IO]
//      fileNameService = new FileNameService()
//      fileNames <- fileNameService.generateLast24Hours
//      contents <- fileNames.traverse(fileName => fileService.readFile(fileName)
//        .map(content => (fileName, content)))
//      state = contents.toMap
//      stateRef <- Ref.of[IO, Map[String, List[String]]](state)
    } yield new DataService(fileService, postgresService, log)
//  } yield new DataService(fileService, postgresService, new FileNameService(), log)
//  } yield new DataService(fileService, postgresService, new FileNameService(), log, stateRef)
//    } yield new DataService(fileService, new FileNameService(), log, stateRef)
  }
}
class DataService private(
                           fileService: FileService,
                           postgresService: PostgresService,
//                           fileNameService: FileNameService,
                           log: Logger[IO],
//                           private val state: Ref[IO, Map[String, List[String]]]
) extends DataServiceTrait {

//  private def logState: IO[Unit] = {
//    state.get.flatMap(currentState => log.info(s"State keys: ${currentState.keys.size}"))
//  }
//
//  private def filterState: IO[Unit] = {
//    fileNameService.generateLast24Hours.flatMap { last24Hours =>
//      state.update(st => st.filterKeys(last24Hours.contains).toMap)
//    }
//  }

  def save(fileName: String, content: String): IO[String] = {
    // TODO replace unsafeRunSync to redeemWith
//    postgresService.save(fileName, content).unsafeRunSync()
//    postgresService.save(fileName, content)

    for {
      result <- fileService.save(fileName, content)
      _ <- postgresService.save(fileName, content)
    } yield result

    // TODO delete this
//    fileService.save(fileName, content)

//    fileService.save(fileName, content).redeemWith(
//      error => IO.raiseError(error),
//      savedFileName => {
//        state.update(st => st.updated(savedFileName, content.split("\n").toList)) *>
//            filterState *>
//            logState.as(savedFileName)
//      }
//    ).onError(error => log.info(s"errr... $error"))
  }

  def readFile(fileName: String): IO[List[String]] = fileService.readFile(fileName)

  def getDateTimeEntries(dateTime: LocalDateTime): IO[List[String]] = postgresService.getDateTimeEntries(dateTime)

  def getInRange(from: LocalDateTime, to: LocalDateTime): IO[List[String]] = fileService.getInRange(from, to)

  def query(userQuery: UserQuery):  IO[Map[String, Option[Aggregate.AggregateValue]]] = postgresService.query(userQuery)

  def getDates: IO[List[LocalDate]] = fileService.getDates

  def getDatesByMonths(monthList: List[LocalDate]): IO[List[LocalDate]] = {
//    fileService.getDatesByMonths(monthList)
    postgresService.getDatesByMonths(monthList)
  }

  def getDateFileNames(date: LocalDate): IO[List[String]] = {
//    fileService.getDateFileNames(date)
    postgresService.getDateFileNames(date)
  }

  // TODO implement getting full data from state
//  def getLast24Hours: IO[List[String]] = {
//    state.get.map(_.keys.toList.sorted)
//  }
}