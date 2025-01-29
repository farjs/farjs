package farjs.filelist

import scommons.react._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../filelist/FileListUi.mjs", JSImport.Default)
object FileListUi extends js.Function1[FileListUiData, ReactClass] {
  
  def apply(data: FileListUiData): ReactClass = js.native
}
