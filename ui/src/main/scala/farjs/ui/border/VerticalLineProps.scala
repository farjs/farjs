package farjs.ui.border

import scommons.react.blessed.BlessedStyle

import scala.scalajs.js

sealed trait VerticalLineProps extends js.Object {
  val left: Int
  val top: Int
  val length: Int
  val lineCh: String
  val style: BlessedStyle
  val startCh: js.UndefOr[String]
  val endCh: js.UndefOr[String]
}

object VerticalLineProps {

  def apply(left: Int,
            top: Int,
            length: Int,
            lineCh: String,
            style: BlessedStyle,
            startCh: js.UndefOr[String] = js.undefined,
            endCh: js.UndefOr[String] = js.undefined): VerticalLineProps = {

    js.Dynamic.literal(
      left = left,
      top = top,
      length = length,
      lineCh = lineCh,
      style = style,
      startCh = startCh,
      endCh = endCh
    ).asInstanceOf[VerticalLineProps]
  }

  def unapply(arg: VerticalLineProps): Option[(Int, Int, Int, String, BlessedStyle, js.UndefOr[String], js.UndefOr[String])] = {
    Some((
      arg.left,
      arg.top,
      arg.length,
      arg.lineCh,
      arg.style,
      arg.startCh,
      arg.endCh
    ))
  }
}
