package scommons.farc.ui.filelist

import io.github.shogowada.scalajs.reactjs.React.Props
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.farc.ui.FarcStateDef
import scommons.react.UiComponent
import scommons.react.redux.BaseStateController

class FileListController(actions: FileListActions, isRight: Boolean)
  extends BaseStateController[FarcStateDef, FileListPanelProps] {

  lazy val uiComponent: UiComponent[FileListPanelProps] = FileListPanel

  def mapStateToProps(dispatch: Dispatch, state: FarcStateDef, props: Props[Unit]): FileListPanelProps = {
    FileListPanelProps(dispatch, actions,
      if (isRight) state.fileListsState.right
      else state.fileListsState.left
    )
  }
}
