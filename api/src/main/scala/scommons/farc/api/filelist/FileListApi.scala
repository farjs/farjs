package scommons.farc.api.filelist

import scala.concurrent.Future

trait FileListApi {

  def currDir: Future[FileListDir]
  
  def changeDir(dir: String): Future[FileListDir]
  
  def listFiles: Future[Seq[FileListItem]]
}
