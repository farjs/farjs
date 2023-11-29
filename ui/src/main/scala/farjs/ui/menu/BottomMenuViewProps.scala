package farjs.ui.menu

import scala.scalajs.js

sealed trait BottomMenuViewProps extends js.Object {
  val width: Int
  val items: js.Array[String]
}

object BottomMenuViewProps {

  def apply(width: Int, items: js.Array[String]): BottomMenuViewProps = {
    js.Dynamic.literal(
      width = width,
      items = items
    ).asInstanceOf[BottomMenuViewProps]
  }

  def unapply(arg: BottomMenuViewProps): Option[(Int, js.Array[String])] = {
    Some((
      arg.width,
      arg.items
    ))
  }
}
