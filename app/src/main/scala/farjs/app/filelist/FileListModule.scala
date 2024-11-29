package farjs.app.filelist

import farjs.app.raw.BetterSqlite3Database
import farjs.app.service.{FolderShortcutsServiceImpl, HistoryProviderImpl}
import farjs.domain.dao._
import farjs.fs.FSServices

class FileListModule(db: BetterSqlite3Database) {

  private val historyKindDao = HistoryKindDao(db)
  val historyProvider = new HistoryProviderImpl(db, historyKindDao)

  val folderShortcutDao: FolderShortcutDao = FolderShortcutDao(db)
  val folderShortcutsService = FolderShortcutsServiceImpl(folderShortcutDao)

  val fsServices = new FSServices(
    folderShortcuts = folderShortcutsService
  )
}
