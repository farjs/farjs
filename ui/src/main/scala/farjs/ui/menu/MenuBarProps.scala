package farjs.ui.menu

import scala.scalajs.js

sealed trait MenuBarProps extends js.Object {
  val items: js.Array[MenuBarItem]
  val onAction: js.Function2[Int, Int, Unit]
  val onClose: js.Function0[Unit]
}

object MenuBarProps {

  def apply(items: js.Array[MenuBarItem],
            onAction: js.Function2[Int, Int, Unit],
            onClose: js.Function0[Unit]): MenuBarProps = {

    js.Dynamic.literal(
      items = items,
      onAction = onAction,
      onClose = onClose
    ).asInstanceOf[MenuBarProps]
  }

  def unapply(arg: MenuBarProps): Option[(js.Array[MenuBarItem], js.Function2[Int, Int, Unit], js.Function0[Unit])] = {
    Some((
      arg.items,
      arg.onAction,
      arg.onClose
    ))
  }
}
