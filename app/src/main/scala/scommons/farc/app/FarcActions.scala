package scommons.farc.app

import scommons.farc.app.filelist.FileListApiImpl
import scommons.farc.ui.filelist.FileListActions

object FarcActions extends FileListActions {

  val api = new FileListApiImpl
}
