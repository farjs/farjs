package farjs.app.filelist

import farjs.app.FarjsStateDef
import farjs.fs.popups.{FSPopups, FSPopupsProps}
import io.github.shogowada.scalajs.reactjs.React.Props
import scommons.react.UiComponent
import scommons.react.redux.{BaseStateController, Dispatch}

object FSPopupsController extends BaseStateController[FarjsStateDef, FSPopupsProps] {

  lazy val uiComponent: UiComponent[FSPopupsProps] = FSPopups

  def mapStateToProps(dispatch: Dispatch, state: FarjsStateDef, props: Props[Unit]): FSPopupsProps = {
    FSPopupsProps(
      dispatch = dispatch,
      popups = state.fsPopups
    )
  }
}
