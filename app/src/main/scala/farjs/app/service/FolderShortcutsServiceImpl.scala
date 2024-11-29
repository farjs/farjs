package farjs.app.service

import farjs.domain.dao.FolderShortcutDao
import farjs.fs.popups.FolderShortcutsService

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../fs/popups/FolderShortcutsService.mjs", JSImport.Default)
object FolderShortcutsServiceImpl extends js.Function1[FolderShortcutDao, FolderShortcutsService] {

  def apply(dao: FolderShortcutDao): FolderShortcutsService = js.native
}
