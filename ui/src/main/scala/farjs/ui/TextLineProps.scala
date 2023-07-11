package farjs.ui

import scommons.react.blessed.BlessedStyle

import scala.scalajs.js

sealed trait TextLineProps extends js.Object {
  val align: TextAlign
  val left: Int
  val top: Int
  val width: Int
  val text: String
  val style: BlessedStyle
  val focused: js.UndefOr[Boolean]
  val padding: js.UndefOr[Int]
}

object TextLineProps {

  def apply(align: TextAlign,
            left: Int,
            top: Int,
            width: Int,
            text: String,
            style: BlessedStyle,
            focused: js.UndefOr[Boolean] = js.undefined,
            padding: js.UndefOr[Int] = js.undefined): TextLineProps = {

    js.Dynamic.literal(
      align = align,
      left = left,
      top = top,
      width = width,
      text = text,
      style = style,
      focused = focused,
      padding = padding
    ).asInstanceOf[TextLineProps]
  }

  def unapply(arg: TextLineProps): Option[(TextAlign, Int, Int, Int, String, BlessedStyle, js.UndefOr[Boolean], js.UndefOr[Int])] = {
    Some((
      arg.align,
      arg.left,
      arg.top,
      arg.width,
      arg.text,
      arg.style,
      arg.focused,
      arg.padding
    ))
  }
}
