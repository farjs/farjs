package farjs.filelist.stack

import farjs.filelist.FileListActions
import farjs.ui.Dispatch
import scommons.react.ReactClass

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
