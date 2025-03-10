package fetch.lvgmc

import cats.effect.IO
import cats.effect.unsafe.implicits.global

object FetchServiceTest {
  def main(args: Array[String]): Unit = {
    fetchFile().unsafeRunSync()
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
}
