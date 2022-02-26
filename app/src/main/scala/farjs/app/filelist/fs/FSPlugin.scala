package farjs.app.filelist.fs

import farjs.filelist._
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import scommons.react.ReactClass
import scommons.react.redux.Dispatch

class FSPlugin(reducer: (FileListState, Any) => FileListState) {

  val component: ReactClass = new FileListPanelController(FSPanel).apply()

  def init(parentDispatch: Dispatch, stack: PanelStack): Unit = {
    stack.updateFor[FileListState](component) { item =>
      PanelStackItem.initDispatch(parentDispatch, reducer, stack, item).copy(
        actions = Some(FSFileListActions),
        state = Some(FileListState(isActive = stack.isActive))
      )
    }
  }
}

object FSPlugin extends FSPlugin(FileListStateReducer.apply)
