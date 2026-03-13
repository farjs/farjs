package farjs.app.filelist

import farjs.app.raw.BetterSqlite3Database
import farjs.filelist.history.HistoryProvider

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../app/filelist/FileListModule.mjs", JSImport.Default)
class FileListModule(db: BetterSqlite3Database) extends js.Object {

  val historyProvider: HistoryProvider = js.native
  val fsServices: FSServices = js.native
}
