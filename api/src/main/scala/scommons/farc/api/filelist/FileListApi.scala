package scommons.farc.api.filelist

import scala.concurrent.Future

trait FileListApi {

  def listFiles(dirUrl: String): Future[Seq[FileListItem]]
}
