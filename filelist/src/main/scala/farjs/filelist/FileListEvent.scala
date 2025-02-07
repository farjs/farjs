package farjs.filelist

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@farjs/filelist/FileListEvent.mjs", JSImport.Default)
object FileListEvent extends js.Object {

  val onFileListCopy: String = js.native
  val onFileListMove: String = js.native
}
