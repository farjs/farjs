package farjs.app.filelist

import farjs.domain.FarjsDBContext
import farjs.domain.dao.HistoryFolderDao
import farjs.filelist.FileListServices

class FileListModule(ctx: FarjsDBContext) {

  val folderDao = new HistoryFolderDao(ctx)
  val folderService = new FileListHistoryServiceImpl(folderDao)

  val services = new FileListServices(folderService)
}
