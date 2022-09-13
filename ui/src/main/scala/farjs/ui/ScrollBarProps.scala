package farjs.ui

import scommons.react.blessed.BlessedStyle

import scala.scalajs.js

sealed trait ScrollBarProps extends js.Object {
  val left: Int
  val top: Int
  val length: Int
  val style: BlessedStyle
  val value: Int
  val extent: Int
  val min: Int
  val max: Int
  val onChange: js.Function1[Int, Unit]
}

object ScrollBarProps {

  def apply(left: Int,
            top: Int,
            length: Int,
            style: BlessedStyle,
            value: Int,
            extent: Int,
            min: Int,
            max: Int,
            onChange: js.Function1[Int, Unit]): ScrollBarProps = {

    js.Dynamic.literal(
      left = left,
      top = top,
      length = length,
      style = style,
      value = value,
      extent = extent,
      min = min,
      max = max,
      onChange = onChange
    ).asInstanceOf[ScrollBarProps]
  }

  def unapply(arg: ScrollBarProps): Option[(Int, Int, Int, BlessedStyle, Int, Int, Int, Int, js.Function1[Int, Unit])] = {
    Some((
      arg.left,
      arg.top,
      arg.length,
      arg.style,
      arg.value,
      arg.extent,
      arg.min,
      arg.max,
      arg.onChange
    ))
  }
}
