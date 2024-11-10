package farjs.app.service

import farjs.app.service.HistoryServiceImpl._
import farjs.domain.HistoryEntity
import farjs.domain.dao.HistoryDao
import farjs.filelist.history.{History, HistoryService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSConverters.JSRichFutureNonThenable
import scala.util.control.NonFatal

class HistoryServiceImpl(dao: HistoryDao) extends HistoryService {

  override def getAll: js.Promise[js.Array[History]] = {
    dao.getAll
      .map(list => js.Array[History](list.map(convertToHistory): _*))
      .recover {
        case NonFatal(ex) =>
          Console.err.println(s"Failed to read all history items, error: $ex")
          js.Array[History]()
      }.toJSPromise
  }

  override def getOne(item: String): js.Promise[js.UndefOr[History]] = {
    dao.getByItem(item)
      .map {
        case Some(h) => convertToHistory(h): js.UndefOr[History]
        case None => js.undefined: js.UndefOr[History]
      }.recover {
        case NonFatal(ex) =>
          Console.err.println(s"Failed to read history item, error: $ex")
          js.undefined: js.UndefOr[History]
      }.toJSPromise
  }

  override def save(h: History): js.Promise[Unit] = {
    dao.save(convertToHistoryEntity(h)).recover {
      case NonFatal(ex) =>
        Console.err.println(s"Failed to save history item, error: $ex")
    }.toJSPromise
  }
}

object HistoryServiceImpl {

  private def convertToHistory(entity: HistoryEntity): History = {
    History(
      item = entity.item,
      params = entity.params match {
        case Some(v) => v
        case None => js.undefined
      }
    )
  }

  private def convertToHistoryEntity(history: History): HistoryEntity = {
    HistoryEntity(
      item = history.item,
      params = history.params.toOption,
      System.currentTimeMillis()
    )
  }
}
