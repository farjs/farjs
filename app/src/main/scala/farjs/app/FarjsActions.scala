package farjs.app

import farjs.app.filelist.FileListApiImpl
import farjs.ui.filelist.FileListActions

object FarjsActions extends FileListActions {

  val api = new FileListApiImpl
}
