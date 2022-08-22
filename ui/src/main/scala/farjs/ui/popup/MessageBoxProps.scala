package farjs.ui.popup

import scommons.react.blessed.BlessedStyle

import scala.scalajs.js

sealed trait MessageBoxProps extends js.Object {
  val title: String
  val message: String
  val actions: js.Array[MessageBoxAction]
  val style: BlessedStyle
}

object MessageBoxProps {

  def apply(title: String,
            message: String,
            actions: js.Array[MessageBoxAction],
            style: BlessedStyle): MessageBoxProps = {

    js.Dynamic.literal(
      title = title,
      message = message,
      actions = actions,
      style = style
    ).asInstanceOf[MessageBoxProps]
  }

  def unapply(arg: MessageBoxProps): Option[(String, String, js.Array[MessageBoxAction], BlessedStyle)] = {
    Some((
      arg.title,
      arg.message,
      arg.actions,
      arg.style
    ))
  }
}
