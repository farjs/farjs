package farjs.filelist.stack

import farjs.filelist.{FileListActions, FileListState}
import farjs.ui.Dispatch
import scommons.react.ReactClass

import scala.scalajs.js

case class PanelStackItem[T](
  component: ReactClass,
  dispatch: Option[Dispatch],
  actions: Option[FileListActions],
  state: Option[T]
) {
  def withState(s: T): PanelStackItem[T] = copy(state = Some(s))
  
  def updateState(f: T => T): PanelStackItem[T] = copy(state = state.map(f))
  
  def getActions: Option[(Dispatch, FileListActions)] = (dispatch, actions) match {
    case (Some(dispatch), Some(actions)) => Some((dispatch, actions))
    case _ => None
  }
}

object PanelStackItem {

  def initDispatch(parentDispatch: Dispatch,
                   reducer: js.Function2[FileListState, js.Any, FileListState],
                   stack: PanelStack,
                   item: PanelStackItem[FileListState]
                  ): PanelStackItem[FileListState] = {

    val dispatch: Any => Any = { action =>
      stack.updateFor[FileListState](item.component) { item =>
        item.updateState { state =>
          reducer(state, action.asInstanceOf[js.Any])
        }
      }
      parentDispatch(action)
    }

    item.copy(dispatch = Some(dispatch))
  }
}
