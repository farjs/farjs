package farjs.app.filelist.service

import farjs.app.filelist.service.FileViewHistoryServiceImpl._
import farjs.domain.FileViewHistoryEntity
import farjs.domain.dao.FileViewHistoryDao
import farjs.file.{FileViewHistory, FileViewHistoryParams, FileViewHistoryService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
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
      params = FileViewHistoryParams(
        isEdit = entity.isEdit,
        encoding = entity.encoding,
        position = entity.position,
        wrap = entity.wrap match {
          case Some(v) => v
          case None => js.undefined
        },
        column = entity.column match {
          case Some(v) => v
          case None => js.undefined
        }
      )
    )
  }

  private def convertToFileViewHistoryEntity(history: FileViewHistory): FileViewHistoryEntity = {
    FileViewHistoryEntity(
      path = history.path,
      isEdit = history.params.isEdit,
      encoding = history.params.encoding,
      position = history.params.position,
      wrap = history.params.wrap.toOption,
      column = history.params.column.toOption,
      System.currentTimeMillis()
    )
  }
}
