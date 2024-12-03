package farjs.app.service

import farjs.app.raw.BetterSqlite3Database
import farjs.domain.dao.HistoryKindDao
import farjs.filelist.history._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../app/service/HistoryProviderImpl.mjs", JSImport.Default)
object HistoryProviderImpl extends js.Function2[BetterSqlite3Database, HistoryKindDao, HistoryProvider] {

  def apply(db: BetterSqlite3Database, kindDao: HistoryKindDao): HistoryProvider = js.native
}
