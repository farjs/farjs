package farjs.api.filelist

import scala.concurrent.Future

trait FileListApi {

  def readDir(parent: Option[String], dir: String): Future[FileListDir]

  def readDir(targetDir: String): Future[FileListDir]
  
  def delete(parent: String, items: Seq[FileListItem]): Future[Unit]

  def mkDir(parent: String, dir: String, multiple: Boolean): Future[String]
}
