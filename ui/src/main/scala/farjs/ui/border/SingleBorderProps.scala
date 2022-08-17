package farjs.ui.border

import scommons.react.blessed.BlessedStyle

import scala.scalajs.js

sealed trait SingleBorderProps extends js.Object {
  val width: Int
  val height: Int
  val style: BlessedStyle
}

object SingleBorderProps {

  def apply(width: Int,
            height: Int,
            style: BlessedStyle): SingleBorderProps = {

    js.Dynamic.literal(
      width = width,
      height = height,
      style = style
    ).asInstanceOf[SingleBorderProps]
  }

  def unapply(arg: SingleBorderProps): Option[(Int, Int, BlessedStyle)] = {
    Some((
      arg.width,
      arg.height,
      arg.style
    ))
  }
}
