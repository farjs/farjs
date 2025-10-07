package farjs.fs

import farjs.filelist.api.FileListApi

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../fs/FSFileListApi.mjs", JSImport.Default)
class FSFileListApi extends FileListApi(js.native, js.native)
