package farjs.app.filelist.zip

import farjs.filelist.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ZipApi(filePath: String) extends FileListApi {

  def readDir(parent: Option[String], dir: String): Future[FileListDir] = Future {
    val root = FileListDir(s"ZIP://$filePath", isRoot = false, List(
      FileListItem("dir 1", isDir = true),
      FileListItem("file 1")
    ))
    parent match {
      case None => root
      case Some(_) if dir == FileListItem.up.name => root
      case Some(path) =>
        FileListDir(s"$path/$dir", isRoot = false, List(
          FileListItem("file 2")
        ))
    }
  }

  def readDir(targetDir: String): Future[FileListDir] = ???

  def delete(parent: String, items: Seq[FileListItem]): Future[Unit] = ???

  def mkDirs(dirs: List[String]): Future[Unit] = ???

  def readFile(parentDirs: List[String], file: FileListItem, position: Double): Future[FileSource] = ???

  def writeFile(parentDirs: List[String], fileName: String, onExists: FileListItem => Future[Option[Boolean]]): Future[Option[FileTarget]] = ???
}
