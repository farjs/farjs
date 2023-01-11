package farjs.ui

import scommons.react.blessed.BlessedStyle

import scala.scalajs.js

sealed trait ListBoxProps extends js.Object {
  val left: Int
  val top: Int
  val width: Int
  val height: Int
  val selected: Int
  val items: js.Array[String]
  val style: BlessedStyle
  val onAction: js.Function1[Int, Unit]
  val onSelect: js.UndefOr[js.Function1[Int, Unit]]
}

object ListBoxProps {

  def apply(left: Int,
            top: Int,
            width: Int,
            height: Int,
            selected: Int,
            items: js.Array[String],
            style: BlessedStyle,
            onAction: js.Function1[Int, Unit],
            onSelect: js.UndefOr[js.Function1[Int, Unit]] = js.undefined): ListBoxProps = {

    js.Dynamic.literal(
      left = left,
      top = top,
      width = width,
      height = height,
      selected = selected,
      items = items,
      style = style,
      onAction = onAction,
      onSelect = onSelect
    ).asInstanceOf[ListBoxProps]
  }

  def unapply(arg: ListBoxProps): Option[(Int, Int, Int, Int, Int, js.Array[String], BlessedStyle, js.Function1[Int, Unit], js.UndefOr[js.Function1[Int, Unit]])] = {
    Some((
      arg.left,
      arg.top,
      arg.width,
      arg.height,
      arg.selected,
      arg.items,
      arg.style,
      arg.onAction,
      arg.onSelect
    ))
  }

  def copy(p: ListBoxProps)(left: Int = p.left,
                            top: Int = p.top,
                            width: Int = p.width,
                            height: Int = p.height,
                            selected: Int = p.selected,
                            items: js.Array[String] = p.items,
                            style: BlessedStyle = p.style,
                            onAction: js.Function1[Int, Unit] = p.onAction,
                            onSelect: js.UndefOr[js.Function1[Int, Unit]] = p.onSelect): ListBoxProps = {

    ListBoxProps(
      left = left,
      top = top,
      width = width,
      height = height,
      selected = selected,
      items = items,
      style = style,
      onAction = onAction,
      onSelect = onSelect
    )
  }
}
