package farjs.filelist.stack

import farjs.filelist.{FileListActions, FileListData, FileListState}
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
  
  def getData: Option[FileListData] = (dispatch, actions, state) match {
    case (Some(dispatch), Some(actions), Some(state)) if FileListState.isFileListState(state.asInstanceOf[js.Any]) =>
      Some(FileListData(dispatch, actions, state.asInstanceOf[FileListState]))
    case _ => None
  }
}
