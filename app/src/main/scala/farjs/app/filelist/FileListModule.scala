package farjs.app.filelist

import farjs.domain.FarjsDBContext
import farjs.domain.dao._
import farjs.filelist.FileListServices

class FileListModule(ctx: FarjsDBContext) {

  val folderDao = new HistoryFolderDao(ctx)
  val folderService = new FileListHistoryServiceImpl(folderDao)

  val mkDirDao = new HistoryMkDirDao(ctx)
  val mkDirService = new FileListHistoryServiceImpl(mkDirDao)

  val services = new FileListServices(
    foldersHistory = folderService,
    mkDirsHistory = mkDirService
  )
}
