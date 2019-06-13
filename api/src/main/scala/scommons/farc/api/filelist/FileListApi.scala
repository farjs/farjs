package scommons.farc.api.filelist

import scala.concurrent.Future

trait FileListApi {

  def rootDir: String
  
  def changeDir(dir: String): Future[String]
  
  def listFiles: Future[Seq[FileListItem]]
}
