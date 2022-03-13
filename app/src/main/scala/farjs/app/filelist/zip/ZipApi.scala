package farjs.app.filelist.zip

import farjs.filelist.api._
import scommons.nodejs.{ChildProcess, raw}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ZipApi(filePath: String,
             rootPath: String,
             entriesF: Future[List[ZipEntry]]) extends FileListApi {

  private lazy val entriesByParentF = entriesF.map(_.groupBy(_.parent))

  def readDir(parent: Option[String], dir: String): Future[FileListDir] = {
    val path = parent.getOrElse(rootPath)
    val targetDir =
      if (dir == FileListItem.up.name) {
        val lastSlash = path.lastIndexOf('/')
        path.take(lastSlash)
      }
      else if (dir == FileListItem.currDir.name) path
      else s"$path/$dir"
    
    readDir(targetDir)
  }

  def readDir(targetDir: String): Future[FileListDir] = {
    entriesByParentF.map { entriesByParent =>
      val path = targetDir.stripPrefix(rootPath).stripPrefix("/")
      val entries = entriesByParent.getOrElse(path, Nil)

      FileListDir(
        path = targetDir,
        isRoot = false,
        items = entries.map(ZipApi.convertToFileListItem)
      )
    }
  }

  def delete(parent: String, items: Seq[FileListItem]): Future[Unit] = ???

  def mkDirs(dirs: List[String]): Future[Unit] = ???

  def readFile(parentDirs: List[String], file: FileListItem, position: Double): Future[FileSource] = ???

  def writeFile(parentDirs: List[String], fileName: String, onExists: FileListItem => Future[Option[Boolean]]): Future[Option[FileTarget]] = ???
}

object ZipApi {

  def convertToFileListItem(zip: ZipEntry): FileListItem = {
    FileListItem(
      name = zip.name,
      isDir = zip.isDir,
      size = zip.size,
      mtimeMs = zip.datetimeMs
    )
  }

  def readZip(childProcess: ChildProcess, filePath: String): Future[List[ZipEntry]] = {
    val (_, future) = childProcess.exec(
      command = s"""unzip -l "$filePath"""",
      options = Some(new raw.ChildProcessOptions {
        override val windowsHide = true
      })
    )

    future.map { case (stdout, _) =>
      val output = stdout.asInstanceOf[String]
      ZipEntry.fromUnzipCommand(output)
    }
  }
}
