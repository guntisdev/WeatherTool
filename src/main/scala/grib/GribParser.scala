package grib

import cats.effect._
import cats.effect.unsafe.implicits.global
import fs2.io.file.{Files, Path}

import java.nio.ByteBuffer

// sbt "runMain grib.GribParser"
object GribParser {
  def parseFile(path: Path): IO[List[Grib]] = {
    def loop(ptr: Long, acc: List[Grib]): IO[List[Grib]] = {
      for {
        fileSize <- Files[IO].size(path)
        result <- if (ptr >= fileSize) {
          IO.pure(acc.reverse)
        } else {
          for {
            grib <- parseGrib(path, ptr)
            nextResult <- loop(ptr + grib.length, grib :: acc)
          } yield nextResult
        }
      } yield result
    }

    loop(0L, List.empty)
  }

  def parseGrib(path: Path, ptr: Long): IO[Grib] = {
    for {
      bytes <- readBytes(path, ptr, 16)
      identifier = new String(bytes.take(4), "ASCII")
      discipline = bytes(6)
      version = bytes(7)
      length = ByteBuffer.wrap(bytes.slice(8, 16)).getLong
      _ <- IO.println(identifier, version, discipline, length)
      grib = Grib(version, discipline, length)
    } yield grib
  }


  def readBytes(path: Path, ptr: Long, len: Int): IO[Array[Byte]] = {
    Files[IO].readRange(path, 4096, ptr, ptr + len)
      .compile
      .to(Array)
  }

  def main(args: Array[String]): Unit = {
    val fileName = "data/HARMONIE_DINI_SF_2025-01-24T030000Z_2025-01-26T010000Z.grib"
    val path = Path(fileName)

    // Example of running the effect
    val program = for {
      _ <- parseFile(path)
    } yield ()

    program.unsafeRunSync()
  }
}