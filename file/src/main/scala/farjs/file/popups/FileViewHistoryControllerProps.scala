package farjs.file.popups

import scala.scalajs.js

sealed trait FileViewHistoryControllerProps extends js.Object {
  val showPopup: Boolean
  val onClose: js.Function0[Unit]
}

object FileViewHistoryControllerProps {

  def apply(showPopup: Boolean,
            onClose: js.Function0[Unit]): FileViewHistoryControllerProps = {

    js.Dynamic.literal(
      showPopup = showPopup,
      onClose = onClose
    ).asInstanceOf[FileViewHistoryControllerProps]
  }

  def unapply(arg: FileViewHistoryControllerProps): Option[(Boolean, js.Function0[Unit])] = {
    Some((
      arg.showPopup ,
      arg.onClose
    ))
  }
}
