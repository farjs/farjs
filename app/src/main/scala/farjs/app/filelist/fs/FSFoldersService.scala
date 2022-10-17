package farjs.app.filelist.fs

import farjs.app.filelist.fs.FSFoldersService._
import farjs.domain.HistoryFolder
import farjs.domain.dao.HistoryFolderDao
import farjs.filelist.history.FileListHistoryService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

class FSFoldersService(dao: HistoryFolderDao) extends FileListHistoryService {

  def getAll: Future[Seq[String]] = {
    dao.getAll.map(_.map(_.path)).recover {
      case NonFatal(ex) =>
        Console.err.println(s"Failed to read history items, error: $ex")
        Nil
    }
  }

  def save(path: String): Future[Unit] = {
    val entity = HistoryFolder(path, System.currentTimeMillis())
    dao.save(entity, maxHistoryItemsCount).recover {
      case NonFatal(ex) =>
        Console.err.println(s"Failed to save history item, error: $ex")
    }
  }
}

object FSFoldersService {
  
  private val maxHistoryItemsCount = 100
}
