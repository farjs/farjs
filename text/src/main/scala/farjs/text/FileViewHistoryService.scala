package farjs.text

import scala.concurrent.Future

case class FileViewHistory(path: String)

trait FileViewHistoryService {

  def getAll: Future[Seq[FileViewHistory]]

  def save(h: FileViewHistory): Future[Unit]
}
