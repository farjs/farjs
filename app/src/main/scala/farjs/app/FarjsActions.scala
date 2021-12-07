package farjs.app

import farjs.app.filelist.FSFileListApi
import farjs.filelist.FileListActions

object FarjsActions extends FileListActions {

  val api = new FSFileListApi
}
