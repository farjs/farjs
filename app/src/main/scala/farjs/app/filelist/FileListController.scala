package farjs.app.filelist

import farjs.app.FarjsStateDef
import farjs.filelist._
import io.github.shogowada.scalajs.reactjs.React.Props
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.react.UiComponent
import scommons.react.redux.BaseStateController

class FileListController(actions: FileListActions, isRight: Boolean)
  extends BaseStateController[FarjsStateDef, FileListPanelProps] {

  lazy val uiComponent: UiComponent[FileListPanelProps] = FileListPanel

  def mapStateToProps(dispatch: Dispatch, state: FarjsStateDef, props: Props[Unit]): FileListPanelProps = {
    FileListPanelProps(dispatch, actions,
      if (isRight) state.fileListsState.right
      else state.fileListsState.left
    )
  }
}
