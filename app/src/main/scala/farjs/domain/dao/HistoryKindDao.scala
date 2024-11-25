package farjs.domain.dao

import farjs.app.raw.BetterSqlite3Database
import farjs.domain._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

trait HistoryKindDao extends js.Object {

  def getAll(): js.Promise[js.Array[HistoryKindEntity]]

  def upsert(entity: HistoryKindEntity): js.Promise[HistoryKindEntity]

  def deleteAll(): js.Promise[Unit]
}

@js.native
@JSImport("../dao/HistoryKindDao.mjs", JSImport.Default)
object HistoryKindDao extends js.Function1[BetterSqlite3Database, HistoryKindDao] {

  def apply(db: BetterSqlite3Database): HistoryKindDao = js.native
}
