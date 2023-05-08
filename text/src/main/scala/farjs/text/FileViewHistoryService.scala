package farjs.text

import scala.concurrent.Future

case class FileViewHistory(path: String,
                           isEdit: Boolean,
                           encoding: String,
                           position: Double,
                           wrap: Option[Boolean],
                           column: Option[Int])

trait FileViewHistoryService {

  def getAll: Future[Seq[FileViewHistory]]

  def getOne(path: String, isEdit: Boolean): Future[Option[FileViewHistory]]

  def save(h: FileViewHistory): Future[Unit]
}
