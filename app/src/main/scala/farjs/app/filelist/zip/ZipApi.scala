package farjs.app.filelist.zip

import farjs.filelist.api._
import scommons.nodejs.{ChildProcess, raw}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ZipApi(childProcess: ChildProcess,
             zipPath: String,
             rootPath: String,
             entriesByParentF: Future[Map[String, List[ZipEntry]]]) extends FileListApi {

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
      mtimeMs = zip.datetimeMs,
      permissions = zip.permissions
    )
  }

  def readZip(childProcess: ChildProcess, zipPath: String): Future[Map[String, List[ZipEntry]]] = {
    val (_, future) = childProcess.exec(
      command = s"""unzip -ZT "$zipPath"""",
      options = Some(new raw.ChildProcessOptions {
        override val windowsHide = true
      })
    )

    future.map { case (stdout, _) =>
      val output = stdout.asInstanceOf[String]
      val entries = ZipEntry.fromUnzipCommand(output)
      ZipApi.groupByParent(entries)
    }
  }
  
  private[zip] def groupByParent(entries: List[ZipEntry]): Map[String, List[ZipEntry]] = {
    
    @annotation.tailrec
    def ensureDirs(entry: ZipEntry, entriesByParent: Map[String, List[ZipEntry]]): Map[String, List[ZipEntry]] = {
      val values = entriesByParent.getOrElse(entry.parent, Nil)
      if (entry.name == "" || values.exists(_.name == entry.name)) entriesByParent
      else {
        val (parent, name) = {
          val lastSlash = entry.parent.lastIndexOf('/')
          if (lastSlash != -1) {
            (entry.parent.take(lastSlash), entry.parent.drop(lastSlash + 1))
          }
          else ("", entry.parent)
        }
        ensureDirs(
          entry = ZipEntry(
            parent = parent,
            name = name,
            isDir = true,
            datetimeMs = entry.datetimeMs,
            permissions = "drw-r--r--"
          ),
          entriesByParent = entriesByParent.updatedWith(entry.parent) {
            case None => Some(entry :: Nil)
            case Some(values) => Some(entry :: values)
          }
        )
      }
    }
    
    var entriesByParent = Map.empty[String, List[ZipEntry]]
    entries.foreach { entry =>
      entriesByParent = ensureDirs(entry, entriesByParent)
    }

    entriesByParent
  }
}
