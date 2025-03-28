package server

import cats.effect.IO
import cats.implicits.toTraverseOps
import fs2.io.file.{Files, Path}

object DebugUtils {
  case class FileInfo(name: String, isDirectory: Boolean)
  case class DirectoryStructure(path: String, files: List[FileInfo])
  case class FolderStructureResponse(
    current: DirectoryStructure,
    data: DirectoryStructure,
    tmp: DirectoryStructure,
    grib: DirectoryStructure
  )

  private def getDirectoryStructure(path: Path): IO[DirectoryStructure] = {
    for {
      absolutePath <- IO(path.toString)
      files <- Files[IO].list(path).compile.toList
      fileInfos <- files.traverse { filePath =>
        Files[IO].isDirectory(filePath).map { isDir =>
          FileInfo(filePath.fileName.toString, isDir)
        }
      }
    } yield DirectoryStructure(absolutePath, fileInfos)
  }

  def getFolderStructure: IO[FolderStructureResponse] = {
    val currentPath = Path(".")
    val dataPath = Path("data")
    val tmpPath = Path("data/tmp")
    val gribPath = Path("data/grib")

    for {
      current <- getDirectoryStructure(currentPath)
      data <- getDirectoryStructure(dataPath)
      tmp <- getDirectoryStructure(tmpPath)
      grib <- getDirectoryStructure(gribPath)
      response = FolderStructureResponse(current, data, tmp, grib)
    } yield response
  }
}