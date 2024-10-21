package farjs.app.filelist.service

import farjs.domain.BaseHistory
import farjs.domain.dao.BaseHistoryDao
import farjs.filelist.history.FileListHistoryService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

class FileListHistoryServiceImpl(dao: BaseHistoryDao)
  extends FileListHistoryService {

  def getAll: Future[Seq[String]] = {
    dao.getAll.map(_.map(_.item)).recover {
      case NonFatal(ex) =>
        Console.err.println(s"Failed to read history items, error: $ex")
        Nil
    }
  }

  def save(path: String): Future[Unit] = {
    val entity = BaseHistory(path, System.currentTimeMillis())
    dao.save(entity).recover {
      case NonFatal(ex) =>
        Console.err.println(s"Failed to save history item, error: $ex")
    }
  }
}
