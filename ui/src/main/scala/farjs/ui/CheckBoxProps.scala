package farjs.ui

import scommons.react.blessed.BlessedStyle

import scala.scalajs.js

sealed trait CheckBoxProps extends js.Object {
  val left: Int
  val top: Int
  val value: Boolean
  val label: String
  val style: BlessedStyle
  val onChange: js.Function0[Unit]
}

object CheckBoxProps {

  def apply(left: Int,
            top: Int,
            value: Boolean,
            label: String,
            style: BlessedStyle,
            onChange: js.Function0[Unit]): CheckBoxProps = {

    js.Dynamic.literal(
      left = left,
      top = top,
      value = value,
      label = label,
      style = style,
      onChange = onChange
    ).asInstanceOf[CheckBoxProps]
  }

  def unapply(arg: CheckBoxProps): Option[(Int, Int, Boolean, String, BlessedStyle, js.Function0[Unit])] = {
    Some((
      arg.left,
      arg.top,
      arg.value,
      arg.label,
      arg.style,
      arg.onChange
    ))
  }
}
