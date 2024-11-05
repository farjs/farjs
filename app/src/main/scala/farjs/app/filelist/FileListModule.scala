package farjs.app.filelist

import farjs.app.filelist.service._
import farjs.domain.FarjsDBContext
import farjs.domain.dao._
import farjs.fs.FSServices

class FileListModule(ctx: FarjsDBContext) {

  val folderDao = new HistoryFolderDao(ctx)
  val folderService = new FileListHistoryServiceImpl(folderDao)

  val mkDirDao = new HistoryMkDirDao(ctx)
  val mkDirService = new FileListHistoryServiceImpl(mkDirDao)

  val selectPatternDao = new HistorySelectPatternDao(ctx)
  val selectPatternService = new FileListHistoryServiceImpl(selectPatternDao)

  val copyItemDao = new HistoryCopyItemDao(ctx)
  val copyItemService = new FileListHistoryServiceImpl(copyItemDao)

  val fileViewHistoryDao = new FileViewHistoryDao(ctx)
  val fileViewHistoryService = new FileViewHistoryServiceImpl(fileViewHistoryDao)

  val historyProvider = new HistoryProviderImpl(
    mkDirService = mkDirService,
    folderService = folderService,
    selectPatternService = selectPatternService,
    copyItemService = copyItemService,
    fileViewHistoryService = fileViewHistoryService
  )

  val folderShortcutDao = new FolderShortcutDao(ctx)
  val folderShortcutsService = new FolderShortcutsServiceImpl(folderShortcutDao)

  val fsServices = new FSServices(
    folderShortcuts = folderShortcutsService
  )
}
