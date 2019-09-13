package farclone.app

import farclone.app.filelist.FileListApiImpl
import farclone.ui.filelist.FileListActions

object FarcActions extends FileListActions {

  val api = new FileListApiImpl
}
