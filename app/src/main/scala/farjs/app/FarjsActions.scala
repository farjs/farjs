package farjs.app

import farjs.app.filelist.FileListApiImpl
import farjs.filelist.FileListActions

object FarjsActions extends FileListActions {

  val api = new FileListApiImpl
}
