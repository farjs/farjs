package farjs.fs.popups

import scommons.react._

object DriveController extends FunctionComponent[DriveControllerProps] {

  private[popups] var drivePopup: UiComponent[DrivePopupProps] = DrivePopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain

    props.showDrivePopupOnLeft.toOption match {
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
