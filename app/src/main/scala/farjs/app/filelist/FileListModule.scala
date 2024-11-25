package farjs.app.filelist

import farjs.app.raw.BetterSqlite3Database
import farjs.app.service.{FolderShortcutsServiceImpl, HistoryProviderImpl}
import farjs.domain.FarjsDBContext
import farjs.domain.dao._
import farjs.fs.FSServices

class FileListModule(db: BetterSqlite3Database, ctx: FarjsDBContext) {

  private val historyKindDao = HistoryKindDao(db)
  val historyProvider = new HistoryProviderImpl(historyKindDao, ctx)

  val folderShortcutDao = new FolderShortcutDao(ctx)
  val folderShortcutsService = new FolderShortcutsServiceImpl(folderShortcutDao)

  val fsServices = new FSServices(
    folderShortcuts = folderShortcutsService
  )
}
