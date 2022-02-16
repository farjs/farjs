package farjs.filelist.fs

import farjs.filelist.stack.PanelStack
import farjs.filelist.{FileListState, FileListsStateReducer}
import scommons.react.ReactClass
import scommons.react.redux.Dispatch

class FSPlugin(reduce: (Boolean, FileListState, Any) => FileListState) {

  val component: ReactClass = FSPanel()

  def init(parentDispatch: Dispatch, isRight: Boolean, stack: PanelStack): Unit = {
    val dispatch: Any => Any = { action =>
      stack.updateFor[FileListState](component) { item =>
        item.updateState { state =>
          reduce(isRight, state, action)
        }
      }
      parentDispatch(action)
    }

    stack.updateFor[FileListState](component) {
      _.withActions(dispatch, FSFileListActions)
        .withState(FileListState(isRight = isRight, isActive = stack.isActive))
    }
  }
}

object FSPlugin extends FSPlugin(FileListsStateReducer.reduceFileList)
