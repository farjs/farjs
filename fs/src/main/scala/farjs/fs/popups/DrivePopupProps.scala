package farjs.fs.popups

import farjs.ui.Dispatch

import scala.scalajs.js

sealed trait DrivePopupProps extends js.Object {
  val dispatch: Dispatch
  val onChangeDir: js.Function1[String, Unit]
  val onClose: js.Function0[Unit]
  val showOnLeft: Boolean
}

object DrivePopupProps {

  def apply(dispatch: Dispatch,
            onChangeDir: js.Function1[String, Unit],
            onClose: js.Function0[Unit],
            showOnLeft: Boolean): DrivePopupProps = {

    js.Dynamic.literal(
      dispatch = dispatch,
      onChangeDir = onChangeDir,
      onClose = onClose,
      showOnLeft = showOnLeft
    ).asInstanceOf[DrivePopupProps]
  }

  def unapply(arg: DrivePopupProps): Option[(Dispatch, js.Function1[String, Unit], js.Function0[Unit], Boolean)] = {
    Some((
      arg.dispatch,
      arg.onChangeDir,
      arg.onClose,
      arg.showOnLeft
    ))
  }
}
