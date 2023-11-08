package farjs.ui.menu

import scala.scalajs.js

sealed trait SubMenuProps extends js.Object {
  val selected: Int
  val items: js.Array[String]
  val top: Int
  val left: Int
  val onClick: js.Function1[Int, Unit]
}

object SubMenuProps {

  def apply(selected: Int,
            items: js.Array[String],
            top: Int,
            left: Int,
            onClick: js.Function1[Int, Unit]): SubMenuProps = {

    js.Dynamic.literal(
      selected = selected,
      items = items,
      top = top,
      left = left,
      onClick = onClick
    ).asInstanceOf[SubMenuProps]
  }

  def unapply(arg: SubMenuProps): Option[(Int, js.Array[String], Int, Int, js.Function1[Int, Unit])] = {
    Some((
      arg.selected,
      arg.items,
      arg.top,
      arg.left,
      arg.onClick
    ))
  }
}
