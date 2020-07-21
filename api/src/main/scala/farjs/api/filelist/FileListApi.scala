package farjs.api.filelist

import scala.concurrent.Future

trait FileListApi {

  def readDir(parent: Option[String], dir: String): Future[FileListDir]
  
  def delete(parent: String, items: Seq[FileListItem]): Future[Unit]
}
