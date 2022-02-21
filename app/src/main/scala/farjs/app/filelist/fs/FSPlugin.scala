package farjs.app.filelist.fs

import farjs.filelist._
import farjs.filelist.stack.PanelStack
import scommons.react.ReactClass
import scommons.react.redux.Dispatch

class FSPlugin(reducer: (FileListState, Any) => FileListState) {

  val component: ReactClass = new FileListPanelController(FSPanel).apply()

  def init(parentDispatch: Dispatch, stack: PanelStack): Unit = {
    val dispatch: Any => Any = { action =>
      stack.updateFor[FileListState](component) { item =>
        item.updateState { state =>
          reducer(state, action)
        }
      }
      parentDispatch(action)
    }

    stack.updateFor[FileListState](component) {
      _.withActions(dispatch, FSFileListActions)
        .withState(FileListState(isActive = stack.isActive))
    }
  }
}

object FSPlugin extends FSPlugin(FileListStateReducer.apply)
