package farjs.domain.dao

import farjs.app.raw.BetterSqlite3Database

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

sealed trait FolderShortcutDao extends js.Object

@js.native
@JSImport("../dao/FolderShortcutDao.mjs", JSImport.Default)
object FolderShortcutDao extends js.Function1[BetterSqlite3Database, FolderShortcutDao] {

  def apply(db: BetterSqlite3Database): FolderShortcutDao = js.native
}
