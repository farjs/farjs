package farjs.app.filelist.fs

import farjs.app.filelist.fs.FSFoldersService._
import farjs.domain.HistoryFolder
import farjs.domain.dao.HistoryFolderDao

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FSFoldersService(dao: HistoryFolderDao) {

  def getAll: Future[Seq[String]] = {
    dao.getAll.map(_.map(_.path))
  }

  def save(path: String): Future[Unit] = {
    val entity = HistoryFolder(path, System.currentTimeMillis())
    dao.save(entity, maxHistoryItemsCount)
  }
}

object FSFoldersService {
  
  private val maxHistoryItemsCount = 100
}
