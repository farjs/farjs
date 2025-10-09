package farjs.fs

import farjs.filelist._
import farjs.filelist.stack._
import farjs.ui.Dispatch
import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../fs/FSPlugin.mjs", JSImport.Default)
class FSPlugin(reducer: js.Function2[FileListState, js.Any, FileListState])
  extends FileListPlugin(js.native) {

  val component: ReactClass = js.native

  def init(parentDispatch: Dispatch, stack: PanelStack): Unit = js.native

  def initDispatch(parentDispatch: Dispatch,
                   reducer: js.Function2[FileListState, js.Any, FileListState],
                   stack: PanelStack,
                   item: PanelStackItem[FileListState]
                  ): PanelStackItem[FileListState] = js.native
}

@js.native
@JSImport("../fs/FSPlugin.mjs", JSImport.Default)
object FSPlugin extends js.Object {

  val instance: FSPlugin = js.native
}
