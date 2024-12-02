package farjs.app.service

import farjs.domain.dao.HistoryDao
import farjs.filelist.history.HistoryService

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../app/service/HistoryServiceImpl.mjs", JSImport.Default)
object HistoryServiceImpl extends js.Function1[HistoryDao, HistoryService] {

  def apply(dao: HistoryDao): HistoryService = js.native
}
