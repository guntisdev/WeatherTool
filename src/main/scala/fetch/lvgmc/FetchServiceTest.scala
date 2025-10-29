package fetch.lvgmc

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import db.{DBConnection, PostgresService}
import fetch.csv.FileNameService

import java.nio.charset.StandardCharsets

object FetchServiceTest {
  def main(args: Array[String]): Unit = {
//    fetchFile().unsafeRunSync()
    fetchAndSave().unsafeRunSync()
  }


//  Eiropa_LTV_pilsetas_nakama_dn.csv
//  Eiropa_LTV_pilsetas_tekosa_dn.csv
//  Latvija_LTV_pilsetas_nakama_dnn.csv
//  Latvija_LTV_pilsetas_tekosa_dn.csv
//  Latvija_faktiskais_laiks.csv

  private def fetchFile(): IO[Unit] = {
    val program = for {
      fetch <- FetchService.of
      result <- fetch.fetchFile("Latvija_LTV_pilsetas_tekosa_dn.csv")
      _ <- IO.println(s"fetched file, size: ${result.length}")
    } yield ()
    program
  }

  private def fetchAndSave (): IO[Unit] = {
    var program = for {
      fileName <- new FileNameService().generateCurrentHour
      _ <- IO.println(fileName)

      fetch <- FetchService.of
      result <- fetch.fetchFile("Latvija_faktiskais_laiks.csv")
      resultStr = new String(result, StandardCharsets.UTF_8)
      _ <- IO.println(s"fetched file, size: ${result.length}")
//      _ <- IO.println(resultStr)

      transactor <- DBConnection.transactor[IO]
      postgresService <- PostgresService.of(transactor)

      _ <- postgresService.save(fileName, resultStr)
    } yield ()
    program
  }


}
