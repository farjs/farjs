package farjs.ui.border

import scommons.react.blessed.BlessedStyle

import scala.scalajs.js

sealed trait DoubleBorderProps extends js.Object {
  val width: Int
  val height: Int
  val style: BlessedStyle
  val left: js.UndefOr[Int]
  val top: js.UndefOr[Int]
  val title: js.UndefOr[String]
  val footer: js.UndefOr[String]
}

object DoubleBorderProps {

  def apply(width: Int,
            height: Int,
            style: BlessedStyle,
            left: js.UndefOr[Int] = js.undefined,
            top: js.UndefOr[Int] = js.undefined,
            title: js.UndefOr[String] = js.undefined,
            footer: js.UndefOr[String] = js.undefined): DoubleBorderProps = {

    js.Dynamic.literal(
      width = width,
      height = height,
      style = style,
      left = left,
      top = top,
      title = title,
      footer = footer
    ).asInstanceOf[DoubleBorderProps]
  }

  def unapply(arg: DoubleBorderProps): Option[
    (Int, Int, BlessedStyle, js.UndefOr[Int], js.UndefOr[Int], js.UndefOr[String], js.UndefOr[String])
  ] = {
    Some((
      arg.width,
      arg.height,
      arg.style,
      arg.left,
      arg.top,
      arg.title,
      arg.footer
    ))
  }
}
