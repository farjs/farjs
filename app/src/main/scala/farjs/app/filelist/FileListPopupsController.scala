package farjs.app.filelist

import farjs.app.FarjsStateDef
import farjs.filelist.popups.{FileListPopups, FileListPopupsProps}
import io.github.shogowada.scalajs.reactjs.React.Props
import scommons.react.UiComponent
import scommons.react.redux._

object FileListPopupsController extends BaseStateController[FarjsStateDef, FileListPopupsProps] {

  lazy val uiComponent: UiComponent[FileListPopupsProps] = FileListPopups

  def mapStateToProps(dispatch: Dispatch, state: FarjsStateDef, props: Props[Unit]): FileListPopupsProps = {
    FileListPopupsProps(
      dispatch = dispatch,
      popups = state.fileListsState.popups
    )
  }
}
