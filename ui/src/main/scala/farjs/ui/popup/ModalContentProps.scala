package farjs.ui.popup

import scommons.react.blessed.{BlessedPadding, BlessedStyle}

import scala.scalajs.js

sealed trait ModalContentProps extends js.Object {
  val title: String
  val width: Int
  val height: Int
  val style: BlessedStyle
  val padding: js.UndefOr[BlessedPadding]
  val left: js.UndefOr[String]
  val footer: js.UndefOr[String]
}

object ModalContentProps {

  def apply(title: String,
            width: Int,
            height: Int,
            style: BlessedStyle,
            padding: js.UndefOr[BlessedPadding] = js.undefined,
            left: js.UndefOr[String] = js.undefined,
            footer: js.UndefOr[String] = js.undefined): ModalContentProps = {

    js.Dynamic.literal(
      title = title,
      width = width,
      height = height,
      style = style,
      padding = padding,
      left = left,
      footer = footer
    ).asInstanceOf[ModalContentProps]
  }

  def unapply(arg: ModalContentProps): Option[
    (String, Int, Int, BlessedStyle, js.UndefOr[BlessedPadding], js.UndefOr[String], js.UndefOr[String])
  ] = {
    Some((
      arg.title,
      arg.width,
      arg.height,
      arg.style,
      arg.padding,
      arg.left,
      arg.footer,
    ))
  }
}
