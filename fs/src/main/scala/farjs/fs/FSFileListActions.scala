package farjs.fs

import farjs.filelist.FileListActions
import farjs.filelist.api.FileListApi

class FSFileListActions private[fs](val api: FileListApi) extends FileListActions

object FSFileListActions extends FSFileListActions(new FSFileListApi)
