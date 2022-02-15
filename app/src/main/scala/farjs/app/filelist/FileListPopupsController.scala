package farjs.app.filelist

import farjs.app.FarjsStateDef
import farjs.filelist.popups.{FileListPopups, FileListPopupsState}
import io.github.shogowada.scalajs.reactjs.React.Props
import scommons.react.UiComponent
import scommons.react.redux._

object FileListPopupsController extends BaseStateController[FarjsStateDef, FileListPopupsState] {

  lazy val uiComponent: UiComponent[FileListPopupsState] = FileListPopups

  def mapStateToProps(dispatch: Dispatch, state: FarjsStateDef, props: Props[Unit]): FileListPopupsState = {
    state.fileListsState.popups
  }
}
