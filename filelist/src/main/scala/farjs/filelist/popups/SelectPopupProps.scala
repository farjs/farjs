package farjs.filelist.popups

import scala.scalajs.js

sealed trait SelectPopupProps extends js.Object {
  val showSelect: Boolean
  val onAction: js.Function1[String, Unit]
  val onCancel: js.Function0[Unit]
}

object SelectPopupProps {

  def apply(showSelect: Boolean,
            onAction: js.Function1[String, Unit],
            onCancel: js.Function0[Unit]): SelectPopupProps = {
    js.Dynamic.literal(
      showSelect = showSelect,
      onAction = onAction,
      onCancel = onCancel
    ).asInstanceOf[SelectPopupProps]
  }

  def unapply(arg: SelectPopupProps): Option[(Boolean, js.Function1[String, Unit], js.Function0[Unit])] = {
    Some((
      arg.showSelect,
      arg.onAction,
      arg.onCancel
    ))
  }
}
