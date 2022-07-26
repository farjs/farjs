package farjs.ui

import scommons.react.blessed.BlessedStyle

import scala.scalajs.js

sealed trait ProgressBarProps extends js.Object {
  val percent: Int
  val left: Int
  val top: Int
  val length: Int
  val style: BlessedStyle
}

object ProgressBarProps {

  def apply(percent: Int,
            left: Int,
            top: Int,
            length: Int,
            style: BlessedStyle): ProgressBarProps = {

    js.Dynamic.literal(
      percent = percent,
      left = left,
      top = top,
      length = length,
      style = style
    ).asInstanceOf[ProgressBarProps]
  }

  def unapply(arg: ProgressBarProps): Option[(Int, Int, Int, Int, BlessedStyle)] = {
    Some((
      arg.percent,
      arg.left,
      arg.top,
      arg.length,
      arg.style
    ))
  }
}
