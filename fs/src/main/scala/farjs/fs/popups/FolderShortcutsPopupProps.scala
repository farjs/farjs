package farjs.fs.popups

import scala.scalajs.js

sealed trait FolderShortcutsPopupProps extends js.Object {
  val onChangeDir: js.Function1[String, Unit]
  val onClose: js.Function0[Unit]
}

object FolderShortcutsPopupProps {

  def apply(onChangeDir: js.Function1[String, Unit],
            onClose: js.Function0[Unit]): FolderShortcutsPopupProps = {

    js.Dynamic.literal(
      onChangeDir = onChangeDir,
      onClose = onClose
    ).asInstanceOf[FolderShortcutsPopupProps]
  }

  def unapply(arg: FolderShortcutsPopupProps): Option[(js.Function1[String, Unit], js.Function0[Unit])] = {
    Some((
      arg.onChangeDir,
      arg.onClose
    ))
  }
}
