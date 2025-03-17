package farjs.fs.popups

import scala.scalajs.js

sealed trait FoldersHistoryPopupProps extends js.Object {
  val onChangeDir: js.Function1[String, Unit]
  val onClose: js.Function0[Unit]
}

object FoldersHistoryPopupProps {

  def apply(onChangeDir: js.Function1[String, Unit],
            onClose: js.Function0[Unit]): FoldersHistoryPopupProps = {

    js.Dynamic.literal(
      onChangeDir = onChangeDir,
      onClose = onClose
    ).asInstanceOf[FoldersHistoryPopupProps]
  }

  def unapply(arg: FoldersHistoryPopupProps): Option[(js.Function1[String, Unit], js.Function0[Unit])] = {
    Some((
      arg.onChangeDir,
      arg.onClose
    ))
  }
}
