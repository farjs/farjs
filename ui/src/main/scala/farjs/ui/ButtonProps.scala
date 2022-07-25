package farjs.ui

import scommons.react.blessed.BlessedStyle

import scala.scalajs.js

sealed trait ButtonProps extends js.Object {
  val left: Int
  val top: Int
  val label: String
  val style: BlessedStyle
  val onPress: js.Function0[Unit]
}

object ButtonProps {

  def apply(left: Int,
            top: Int,
            label: String,
            style: BlessedStyle,
            onPress: js.Function0[Unit]): ButtonProps = {

    js.Dynamic.literal(
      left = left,
      top = top,
      label = label,
      style = style,
      onPress = onPress
    ).asInstanceOf[ButtonProps]
  }

  def unapply(arg: ButtonProps): Option[(Int, Int, String, BlessedStyle, js.Function0[Unit])] = {
    Some((
      arg.left,
      arg.top,
      arg.label,
      arg.style,
      arg.onPress
    ))
  }
}
