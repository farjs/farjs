package farjs.fs.popups

import farjs.fs.popups.FSPopupsActions._
import scommons.react._
import scommons.react.redux.Dispatch

case class DriveControllerProps(dispatch: Dispatch,
                                show: DrivePopupShow,
                                onChangeDir: (String, Boolean) => Unit)

object DriveController extends FunctionComponent[DriveControllerProps] {

  private[popups] var drivePopup: UiComponent[DrivePopupProps] = DrivePopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val showOnLeft = props.show == ShowDriveOnLeft

    if (props.show != DrivePopupHidden) {
      <(drivePopup())(^.wrapped := DrivePopupProps(
        dispatch = props.dispatch,
        onChangeDir = { dir =>
          props.dispatch(DrivePopupAction(show = DrivePopupHidden))
          props.onChangeDir(dir, showOnLeft)
        },
        onClose = { () =>
          props.dispatch(DrivePopupAction(show = DrivePopupHidden))
        },
        showOnLeft = showOnLeft
      ))()
    }
    else null
  }
}
