package farjs.filelist.stack

import farjs.filelist.FileListActions
import scommons.react.ReactClass
import scommons.react.redux.Dispatch

case class PanelStackItem[T](
  component: ReactClass,
  dispatch: Option[Dispatch],
  actions: Option[FileListActions],
  state: Option[T]
) {
  def withActions(dispatch: Dispatch, actions: FileListActions): PanelStackItem[T] =
    copy(dispatch = Some(dispatch), actions = Some(actions))
  
  def withState(s: T): PanelStackItem[T] = copy(state = Some(s))
  
  def getActions: Option[(Dispatch, FileListActions)] = (dispatch, actions) match {
    case (Some(dispatch), Some(actions)) => Some((dispatch, actions))
    case _ => None
  }
}
