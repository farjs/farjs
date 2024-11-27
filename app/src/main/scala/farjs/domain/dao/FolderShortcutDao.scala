package farjs.domain.dao

import farjs.app.raw.BetterSqlite3Database
import farjs.domain._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

trait FolderShortcutDao extends js.Object {

  def getAll(): js.Promise[js.Array[FolderShortcut]]

  def save(entity: FolderShortcut): js.Promise[Unit]

  def delete(id: Int): js.Promise[Unit]

  def deleteAll(): js.Promise[Unit]
}

@js.native
@JSImport("../dao/FolderShortcutDao.mjs", JSImport.Default)
object FolderShortcutDao extends js.Function1[BetterSqlite3Database, FolderShortcutDao] {

  def apply(db: BetterSqlite3Database): FolderShortcutDao = js.native
}
