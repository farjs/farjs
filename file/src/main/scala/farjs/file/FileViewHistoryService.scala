package farjs.file

import scala.concurrent.Future

case class FileViewHistory(path: String, params: FileViewHistoryParams)

trait FileViewHistoryService {

  def getAll: Future[Seq[FileViewHistory]]

  def getOne(path: String, isEdit: Boolean): Future[Option[FileViewHistory]]

  def save(h: FileViewHistory): Future[Unit]
}
