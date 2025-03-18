package farjs.fs.popups

import scala.scalajs.js

sealed trait FolderShortcutsControllerProps extends js.Object {
  val showPopup: Boolean
  val onChangeDir: js.Function1[String, Unit]
  val onClose: js.Function0[Unit]
}

object FolderShortcutsControllerProps {

  def apply(showPopup: Boolean,
            onChangeDir: js.Function1[String, Unit],
            onClose: js.Function0[Unit]): FolderShortcutsControllerProps = {

    js.Dynamic.literal(
      showPopup = showPopup,
      onChangeDir = onChangeDir,
      onClose = onClose
    ).asInstanceOf[FolderShortcutsControllerProps]
  }

  def unapply(arg: FolderShortcutsControllerProps): Option[(Boolean, js.Function1[String, Unit], js.Function0[Unit])] = {
    Some((
      arg.showPopup,
      arg.onChangeDir,
      arg.onClose
    ))
  }
}
