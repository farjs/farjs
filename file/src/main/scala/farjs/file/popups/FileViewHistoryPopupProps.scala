package farjs.file.popups

import farjs.file.FileViewHistory

import scala.scalajs.js

sealed trait FileViewHistoryPopupProps extends js.Object {
  val onAction: js.Function1[FileViewHistory, Unit]
  val onClose: js.Function0[Unit]
}

object FileViewHistoryPopupProps {

  def apply(onAction: js.Function1[FileViewHistory, Unit],
            onClose: js.Function0[Unit]): FileViewHistoryPopupProps = {

    js.Dynamic.literal(
      onAction = onAction,
      onClose = onClose
    ).asInstanceOf[FileViewHistoryPopupProps]
  }

  def unapply(arg: FileViewHistoryPopupProps): Option[(js.Function1[FileViewHistory, Unit], js.Function0[Unit])] = {
    Some((
      arg.onAction,
      arg.onClose
    ))
  }
}
