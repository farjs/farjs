package scommons.farc.ui.filelist

import io.github.shogowada.scalajs.reactjs.React.Props
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.farc.ui.FarcStateDef
import scommons.react.UiComponent
import scommons.react.redux.BaseStateController

class FileListController(actions: FileListActions)
  extends BaseStateController[FarcStateDef, FilePanelProps] {

  lazy val uiComponent: UiComponent[FilePanelProps] = FilePanel

  def mapStateToProps(dispatch: Dispatch, state: FarcStateDef, props: Props[Unit]): FilePanelProps = {
    FilePanelProps(dispatch, actions, state.fileListState)
  }
}
