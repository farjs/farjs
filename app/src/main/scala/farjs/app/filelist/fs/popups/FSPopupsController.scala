package farjs.app.filelist.fs.popups

import farjs.app.FarjsStateDef
import io.github.shogowada.scalajs.reactjs.React.Props
import scommons.react.UiComponent
import scommons.react.redux._

object FSPopupsController extends BaseStateController[FarjsStateDef, FSPopupsProps] {

  lazy val uiComponent: UiComponent[FSPopupsProps] = FSPopups

  def mapStateToProps(dispatch: Dispatch, state: FarjsStateDef, props: Props[Unit]): FSPopupsProps = {
    FSPopupsProps(
      dispatch = dispatch,
      popups = state.fsPopups
    )
  }
}
