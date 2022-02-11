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
  def withState(s: T): PanelStackItem[T] = copy(state = Some(s))
}
