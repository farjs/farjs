package farjs.ui.filelist.popups

import io.github.shogowada.scalajs.reactjs.React.Props
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import farjs.ui.FarjsStateDef
import farjs.ui.filelist.FileListActions
import scommons.react.UiComponent
import scommons.react.redux.BaseStateController

class FileListPopupsController(actions: FileListActions)
  extends BaseStateController[FarjsStateDef, FileListPopupsProps] {

  lazy val uiComponent: UiComponent[FileListPopupsProps] = FileListPopups

  def mapStateToProps(dispatch: Dispatch, state: FarjsStateDef, props: Props[Unit]): FileListPopupsProps = {
    FileListPopupsProps(dispatch, actions, state.fileListsState)
  }
}
