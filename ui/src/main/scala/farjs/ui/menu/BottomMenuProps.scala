package farjs.ui.menu

import scala.scalajs.js

sealed trait BottomMenuProps extends js.Object {
  val items: js.Array[String]
}

object BottomMenuProps {

  def apply(items: js.Array[String]): BottomMenuProps = {
    js.Dynamic.literal(
      items = items
    ).asInstanceOf[BottomMenuProps]
  }

  def unapply(arg: BottomMenuProps): Option[js.Array[String]] = {
    Some(
      arg.items
    )
  }
}
