package farjs.ui.popup

import scommons.react.blessed.BlessedStyle

import scala.scalajs.js

sealed trait ModalProps extends js.Object {
  val title: String
  val width: Int
  val height: Int
  val style: BlessedStyle
  val onCancel: js.Function0[Unit]
}

object ModalProps {

  def apply(title: String,
            width: Int,
            height: Int,
            style: BlessedStyle,
            onCancel: js.Function0[Unit]): ModalProps = {

    js.Dynamic.literal(
      title = title,
      width = width,
      height = height,
      style = style,
      onCancel = onCancel
    ).asInstanceOf[ModalProps]
  }

  def unapply(arg: ModalProps): Option[(String, Int, Int, BlessedStyle, js.Function0[Unit])] = {
    Some((
      arg.title,
      arg.width,
      arg.height,
      arg.style,
      arg.onCancel
    ))
  }
}
