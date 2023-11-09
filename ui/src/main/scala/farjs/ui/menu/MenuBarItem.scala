package farjs.ui.menu

import scala.scalajs.js

sealed trait MenuBarItem extends js.Object {
  val label: String
  val subItems: js.Array[String]
}

object MenuBarItem {

  def apply(label: String, subItems: js.Array[String]): MenuBarItem = {
    js.Dynamic.literal(
      label = label,
      subItems = subItems
    ).asInstanceOf[MenuBarItem]
  }
}
