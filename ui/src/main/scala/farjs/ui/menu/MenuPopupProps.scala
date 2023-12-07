package farjs.ui.menu

import scala.scalajs.js

sealed trait MenuPopupProps extends js.Object {
  val title: String
  val items: js.Array[String]
  val getLeft: js.Function1[Int, String]
  val onSelect: js.Function1[Int, Unit]
  val onClose: js.Function0[Unit]
}

object MenuPopupProps {

  def apply(title: String,
            items: js.Array[String],
            getLeft: js.Function1[Int, String],
            onSelect: js.Function1[Int, Unit],
            onClose: js.Function0[Unit]): MenuPopupProps = {

    js.Dynamic.literal(
      title = title,
      items = items,
      getLeft = getLeft,
      onSelect = onSelect,
      onClose = onClose
    ).asInstanceOf[MenuPopupProps]
  }

  def unapply(arg: MenuPopupProps): Option[(String, js.Array[String], js.Function1[Int, String], js.Function1[Int, Unit], js.Function0[Unit])] = {
    Some((
      arg.title,
      arg.items,
      arg.getLeft,
      arg.onSelect,
      arg.onClose
    ))
  }
}
