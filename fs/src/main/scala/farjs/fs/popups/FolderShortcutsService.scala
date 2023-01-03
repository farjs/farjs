package farjs.fs.popups

import scala.concurrent.Future

trait FolderShortcutsService {

  def getAll: Future[Seq[Option[String]]]

  def save(index: Int, path: String): Future[Unit]

  def delete(index: Int): Future[Unit]
}
