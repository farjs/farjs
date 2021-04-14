package farjs.filelist.api

import scala.concurrent.Future

trait FileListApi {

  def readDir(parent: Option[String], dir: String): Future[FileListDir]

  def readDir(targetDir: String): Future[FileListDir]
  
  def delete(parent: String, items: Seq[FileListItem]): Future[Unit]

  def mkDirs(dirs: List[String]): Future[Unit]

  def readFile(parentDirs: List[String], file: FileListItem, position: Double): Future[FileSource]

  def writeFile(parentDirs: List[String],
                fileName: String,
                onExists: FileListItem => Future[Option[Boolean]]): Future[Option[FileTarget]]
}
