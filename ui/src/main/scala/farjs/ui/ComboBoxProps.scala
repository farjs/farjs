package farjs.ui

import scala.scalajs.js

sealed trait ComboBoxProps extends js.Object {
  val left: Int
  val top: Int
  val width: Int
  val items: js.Array[String]
  val value: String
  val onChange: js.Function1[String, Unit]
  val onEnter: js.UndefOr[js.Function0[Unit]]
}

object ComboBoxProps {

  def apply(left: Int,
            top: Int,
            width: Int,
            items: js.Array[String],
            value: String,
            onChange: js.Function1[String, Unit],
            onEnter: js.UndefOr[js.Function0[Unit]] = js.undefined): ComboBoxProps = {

    js.Dynamic.literal(
      left = left,
      top = top,
      width = width,
      items = items,
      value = value,
      onChange = onChange,
      onEnter = onEnter
    ).asInstanceOf[ComboBoxProps]
  }

  def unapply(arg: ComboBoxProps): Option[(Int, Int, Int, js.Array[String], String, js.Function1[String, Unit], js.UndefOr[js.Function0[Unit]])] = {
    Some((
      arg.left,
      arg.top,
      arg.width,
      arg.items,
      arg.value,
      arg.onChange,
      arg.onEnter
    ))
  }
}
