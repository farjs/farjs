package farjs.ui

import scommons.react.blessed.BlessedStyle

import scala.scalajs.js

sealed trait ComboBoxPopupProps extends js.Object {
  val left: Int
  val top: Int
  val width: Int
  val items: js.Array[String]
  val viewport: ListViewport
  val setViewport: js.Function1[ListViewport, Unit]
  val style: BlessedStyle
  val onClick: js.Function1[Int, Unit]
}

object ComboBoxPopupProps {

  def apply(left: Int,
            top: Int,
            width: Int,
            items: js.Array[String],
            viewport: ListViewport,
            setViewport: js.Function1[ListViewport, Unit],
            style: BlessedStyle,
            onClick: js.Function1[Int, Unit]): ComboBoxPopupProps = {

    js.Dynamic.literal(
      left = left,
      top = top,
      width = width,
      items = items,
      viewport = viewport,
      setViewport = setViewport,
      style = style,
      onClick = onClick
    ).asInstanceOf[ComboBoxPopupProps]
  }

  def unapply(arg: ComboBoxPopupProps): Option[(Int, Int, Int, js.Array[String], ListViewport, js.Function1[ListViewport, Unit], BlessedStyle, js.Function1[Int, Unit])] = {
    Some((
      arg.left,
      arg.top,
      arg.width,
      arg.items,
      arg.viewport,
      arg.setViewport,
      arg.style,
      arg.onClick
    ))
  }
}
