package farjs.domain.dao

import farjs.app.raw.BetterSqlite3Database
import farjs.domain._
import farjs.filelist.history.History

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

trait HistoryDao extends js.Object {

  def getAll(): js.Promise[js.Array[History]]

  def getByItem(item: String): js.Promise[js.UndefOr[History]]

  def save(entity: History, updatedAt: Double): js.Promise[Unit]

  def deleteAll(): js.Promise[Unit]
}

@js.native
@JSImport("../dao/HistoryDao.mjs", JSImport.Default)
object HistoryDao extends js.Function3[BetterSqlite3Database, HistoryKindEntity, Int, HistoryDao] {

  def apply(db: BetterSqlite3Database, kind: HistoryKindEntity, maxItemsCount: Int): HistoryDao = js.native
}
