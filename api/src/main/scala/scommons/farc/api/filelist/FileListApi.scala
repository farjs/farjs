package scommons.farc.api.filelist

import scala.concurrent.Future

trait FileListApi {

  def listFiles(dir: String): Future[Seq[FileListItem]]
}
