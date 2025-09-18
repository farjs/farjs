package farjs.fs.popups

import scommons.react._

object DriveController extends FunctionComponent[DriveControllerProps] {

  private[popups] var drivePopup: ReactClass = DrivePopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain

    props.showDrivePopupOnLeft.toOption match {
      case Some(showOnLeft) =>
        <(drivePopup)(^.plain := DrivePopupProps(
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
