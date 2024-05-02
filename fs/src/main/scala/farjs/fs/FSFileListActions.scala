package farjs.fs

import farjs.filelist.FileListActions
import farjs.filelist.api.FileListApi

class FSFileListActions private[fs](api: FileListApi) extends FileListActions(api)

object FSFileListActions extends FSFileListActions(new FSFileListApi)
