package farjs.app.service

import farjs.app.filelist.FileListRoot.FolderShortcutsService
import farjs.domain.dao.FolderShortcutDao

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../fs/popups/FolderShortcutsService.mjs", JSImport.Default)
object FolderShortcutsServiceImpl extends js.Function1[FolderShortcutDao, FolderShortcutsService] {

  def apply(dao: FolderShortcutDao): FolderShortcutsService = js.native
}
