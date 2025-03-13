package farjs.fs.popups

import farjs.ui.Dispatch

import scala.scalajs.js

sealed trait DriveControllerProps extends js.Object {
  val dispatch: Dispatch
  val showDrivePopupOnLeft: js.UndefOr[Boolean]
  val onChangeDir: js.Function2[String, Boolean, Unit]
  val onClose: js.Function0[Unit]
}

object DriveControllerProps {

  def apply(dispatch: Dispatch,
            showDrivePopupOnLeft: js.UndefOr[Boolean],
            onChangeDir: js.Function2[String, Boolean, Unit],
            onClose: js.Function0[Unit]): DriveControllerProps = {

    js.Dynamic.literal(
      dispatch = dispatch,
      showDrivePopupOnLeft = showDrivePopupOnLeft,
      onChangeDir = onChangeDir,
      onClose = onClose
    ).asInstanceOf[DriveControllerProps]
  }

  def unapply(arg: DriveControllerProps): Option[(Dispatch, js.UndefOr[Boolean], js.Function2[String, Boolean, Unit], js.Function0[Unit])] = {
    Some((
      arg.dispatch,
      arg.showDrivePopupOnLeft,
      arg.onChangeDir,
      arg.onClose
    ))
  }
}
