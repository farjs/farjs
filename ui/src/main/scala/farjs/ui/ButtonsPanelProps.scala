package farjs.ui

import scommons.react.blessed.BlessedStyle

import scala.scalajs.js

sealed trait ButtonsPanelProps extends js.Object {
  val top: Int
  val actions: js.Array[ButtonsPanelAction]
  val style: BlessedStyle
  val padding: js.UndefOr[Int]
  val margin: js.UndefOr[Int]
}

object ButtonsPanelProps {

  def apply(top: Int,
            actions: js.Array[ButtonsPanelAction],
            style: BlessedStyle,
            padding: js.UndefOr[Int] = js.undefined,
            margin: js.UndefOr[Int] = js.undefined): ButtonsPanelProps = {

    js.Dynamic.literal(
      top = top,
      actions = actions,
      style = style,
      padding = padding,
      margin = margin
    ).asInstanceOf[ButtonsPanelProps]
  }

  def unapply(arg: ButtonsPanelProps): Option[(Int, js.Array[ButtonsPanelAction], BlessedStyle, js.UndefOr[Int], js.UndefOr[Int])] = {
    Some((
      arg.top,
      arg.actions,
      arg.style,
      arg.padding,
      arg.margin
    ))
  }
}
