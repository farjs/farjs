package farjs.filelist.history

import scala.concurrent.Future

trait FileListHistoryService {

  def getAll: Future[Seq[String]]

  def save(path: String): Future[Unit]
}
