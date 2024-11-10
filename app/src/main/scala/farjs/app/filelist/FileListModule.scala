package farjs.app.filelist

import farjs.app.filelist.service._
import farjs.domain.FarjsDBContext
import farjs.domain.dao._
import farjs.fs.FSServices

class FileListModule(ctx: FarjsDBContext) {

  val historyKindDao = new HistoryKindDao(ctx)
  val historyProvider = new HistoryProviderImpl(historyKindDao)

  val folderShortcutDao = new FolderShortcutDao(ctx)
  val folderShortcutsService = new FolderShortcutsServiceImpl(folderShortcutDao)

  val fsServices = new FSServices(
    folderShortcuts = folderShortcutsService
  )
}
