package scommons.farc.ui.filelist.popups

import io.github.shogowada.scalajs.reactjs.React.Props
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.farc.ui.FarcStateDef
import scommons.react.UiComponent
import scommons.react.redux.BaseStateController

object FileListPopupsController extends BaseStateController[FarcStateDef, FileListPopupsProps] {

  lazy val uiComponent: UiComponent[FileListPopupsProps] = FileListPopups

  def mapStateToProps(dispatch: Dispatch, state: FarcStateDef, props: Props[Unit]): FileListPopupsProps = {
    FileListPopupsProps(dispatch, state.fileListsState.popups)
  }
}
