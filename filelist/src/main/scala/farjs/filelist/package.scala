package farjs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object filelist {

  @js.native
  @JSImport("@farjs/filelist/FileListStateReducer.mjs", JSImport.Default)
  object FileListStateReducer extends js.Function2[FileListState, js.Any, FileListState] {

    def apply(state: FileListState, action: js.Any): FileListState = js.native
  }
}
