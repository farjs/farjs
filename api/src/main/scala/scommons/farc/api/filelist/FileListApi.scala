package scommons.farc.api.filelist

import scala.concurrent.Future

trait FileListApi {

  def readDir(parent: Option[String], dir: String): Future[FileListDir]
}
