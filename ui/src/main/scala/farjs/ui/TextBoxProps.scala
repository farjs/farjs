package farjs.ui

import scala.scalajs.js

sealed trait TextBoxProps extends js.Object {
  val left: Int
  val top: Int
  val width: Int
  val value: String
  val onChange: js.Function1[String, Unit]
  val onEnter: js.UndefOr[js.Function0[Unit]]
}

object TextBoxProps {

  def apply(left: Int,
            top: Int,
            width: Int,
            value: String,
            onChange: js.Function1[String, Unit],
            onEnter: js.UndefOr[js.Function0[Unit]] = js.undefined): TextBoxProps = {

    js.Dynamic.literal(
      left = left,
      top = top,
      width = width,
      value = value,
      onChange = onChange,
      onEnter = onEnter
    ).asInstanceOf[TextBoxProps]
  }

  def unapply(arg: TextBoxProps): Option[(Int, Int, Int, String, js.Function1[String, Unit], js.UndefOr[js.Function0[Unit]])] = {
    Some((
      arg.left,
      arg.top,
      arg.width,
      arg.value,
      arg.onChange,
      arg.onEnter
    ))
  }
}
