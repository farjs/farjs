package farclone.ui.filelist.popups

import io.github.shogowada.scalajs.reactjs.React.Props
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import farclone.ui.FarcStateDef
import farclone.ui.filelist.FileListActions
import scommons.react.UiComponent
import scommons.react.redux.BaseStateController

class FileListPopupsController(actions: FileListActions)
  extends BaseStateController[FarcStateDef, FileListPopupsProps] {

  lazy val uiComponent: UiComponent[FileListPopupsProps] = FileListPopups

  def mapStateToProps(dispatch: Dispatch, state: FarcStateDef, props: Props[Unit]): FileListPopupsProps = {
    FileListPopupsProps(dispatch, actions, state.fileListsState)
  }
}
