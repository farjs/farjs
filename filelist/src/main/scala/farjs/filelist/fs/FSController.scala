package farjs.filelist.fs

import farjs.filelist._
import io.github.shogowada.scalajs.reactjs.React.Props
import scommons.react.UiComponent
import scommons.react.redux._

class FSController(actions: FileListActions)
  extends BaseStateController[FileListsGlobalState, FSPanelProps] {

  lazy val uiComponent: UiComponent[FSPanelProps] = FSPanel

  def mapStateToProps(dispatch: Dispatch, state: FileListsGlobalState, props: Props[Unit]): FSPanelProps = {
    FSPanelProps(
      dispatch = dispatch,
      actions = actions,
      data = state.fileListsState
    )
  }
}
