package farjs.app.filelist.service

import farjs.app.filelist.service.FileViewHistoryServiceImpl._
import farjs.domain.FileViewHistoryEntity
import farjs.domain.dao.FileViewHistoryDao
import farjs.text.{FileViewHistory, FileViewHistoryService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

class FileViewHistoryServiceImpl(dao: FileViewHistoryDao) extends FileViewHistoryService {

  def getAll: Future[Seq[FileViewHistory]] = {
    dao.getAll
      .map(_.map(convertToFileViewHistory))
      .recover {
        case NonFatal(ex) =>
          Console.err.println(s"Failed to read all file view history, error: $ex")
          Nil
      }
  }

  def getOne(path: String, isEdit: Boolean): Future[Option[FileViewHistory]] = {
    dao.getById(path, isEdit)
      .map(_.map(convertToFileViewHistory))
      .recover {
        case NonFatal(ex) =>
          Console.err.println(s"Failed to read one file view history, error: $ex")
          None
      }
  }

  def save(h: FileViewHistory): Future[Unit] = {
    dao.save(convertToFileViewHistoryEntity(h)).recover {
      case NonFatal(ex) =>
        Console.err.println(s"Failed to save file view history, error: $ex")
    }
  }
}

object FileViewHistoryServiceImpl {
  
  private def convertToFileViewHistory(entity: FileViewHistoryEntity): FileViewHistory = {
    FileViewHistory(
      path = entity.path,
      isEdit = entity.isEdit,
      encoding = entity.encoding,
      position = entity.position,
      wrap = entity.wrap,
      column = entity.column
    )
  }

  private def convertToFileViewHistoryEntity(history: FileViewHistory): FileViewHistoryEntity = {
    FileViewHistoryEntity(
      path = history.path,
      isEdit = history.isEdit,
      encoding = history.encoding,
      position = history.position,
      wrap = history.wrap,
      column = history.column,
      System.currentTimeMillis()
    )
  }
}
