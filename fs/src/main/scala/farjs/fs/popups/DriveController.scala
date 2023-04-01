package farjs.fs.popups

import farjs.ui.Dispatch
import scommons.react._

import scala.scalajs.js

case class DriveControllerProps(dispatch: Dispatch,
                                showDrivePopupOnLeft: Option[Boolean],
                                onChangeDir: (String, Boolean) => Unit,
                                onClose: js.Function0[Unit])

object DriveController extends FunctionComponent[DriveControllerProps] {

  private[popups] var drivePopup: UiComponent[DrivePopupProps] = DrivePopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    props.showDrivePopupOnLeft match {
      case Some(showOnLeft) =>
        <(drivePopup())(^.wrapped := DrivePopupProps(
          dispatch = props.dispatch,
          onChangeDir = { dir =>
            props.onClose()
            props.onChangeDir(dir, showOnLeft)
          },
          onClose = props.onClose,
          showOnLeft = showOnLeft
        ))()
      case None => null
    }
  }
}
