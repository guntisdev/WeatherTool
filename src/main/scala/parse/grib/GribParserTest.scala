package parse.grib

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import data.DataService
import fs2.io.file.Path
import io.circe.generic.auto._
import io.circe.syntax._

object GribParserTest {
  def main(args: Array[String]): Unit = {
    val gribTitle = Codes.codesToString(0, 0, 2)
    println(gribTitle)

    val program = for {
      dataService <- DataService.of
      fileName = s"${dataService.GRIB_FOLDER}/harmonie_2025-02-01T1500Z_2025-02-01T180000Z.grib"
      path = Path(fileName)
      gribList <- GribParser.parseFile(path)
      json = gribList.asJson
      _ <- IO.println(json.spaces2)
    } yield ()

    program.unsafeRunSync()
  }
}
