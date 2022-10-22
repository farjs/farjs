package farjs.app.filelist

import farjs.app.filelist.history.FileListHistoryServiceImpl
import farjs.domain.FarjsDBContext
import farjs.domain.dao._
import farjs.filelist.FileListServices

class FileListModule(ctx: FarjsDBContext) {

  val folderDao = new HistoryFolderDao(ctx)
  val folderService = new FileListHistoryServiceImpl(folderDao)

  val mkDirDao = new HistoryMkDirDao(ctx)
  val mkDirService = new FileListHistoryServiceImpl(mkDirDao)

  val selectPatternDao = new HistorySelectPatternDao(ctx)
  val selectPatternService = new FileListHistoryServiceImpl(selectPatternDao)

  val copyItemDao = new HistoryCopyItemDao(ctx)
  val copyItemService = new FileListHistoryServiceImpl(copyItemDao)

  val services = new FileListServices(
    foldersHistory = folderService,
    mkDirsHistory = mkDirService,
    selectPatternsHistory = selectPatternService,
    copyItemsHistory = copyItemService
  )
}
