package grib

import cats.effect.IO
import fs2.io.file.Path
import io.circe.generic.auto._
import io.circe.syntax._
import cats.effect.unsafe.implicits.global

object GribParserTest {
  def main(args: Array[String]): Unit = {
    val gribTitle = Codes.codesToString(0, 0, 2)
    println(gribTitle)


    val fileName = "data/HARMONIE_DINI_SF_2025-01-24T030000Z_2025-01-26T010000Z.grib"
    val path = Path(fileName)

    val program = for {
      gribList <- GribParser.parseFile(path)
      json = gribList.asJson
      _ <- IO.println(json.spaces2)
    } yield ()

    program.unsafeRunSync()
  }
}
