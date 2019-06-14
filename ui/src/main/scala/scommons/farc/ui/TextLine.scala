package scommons.farc.ui

import scommons.farc.ui.TextLine.TextAlign
import scommons.react._
import scommons.react.blessed._

case class TextLineProps(align: TextAlign,
                         pos: (Int, Int),
                         width: Int,
                         text: String,
                         style: BlessedStyle,
                         focused: Boolean = false,
                         padding: Int = 1)

object TextLine extends FunctionComponent[TextLineProps] {
  
  sealed trait TextAlign
  case object Left extends TextAlign
  case object Center extends TextAlign
  case object Right extends TextAlign
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (left, top) = props.pos
    val padding = " " * props.padding
    val text = {
      val paddingLen = props.padding * 2
      val dx = props.text.length - (props.width - paddingLen)
      if (dx > 0) {
        val prefix = props.text.take(3)
        val sufix = props.text.drop(dx + 6)
        s"$padding$prefix...$sufix$padding"
      }
      else s"$padding${props.text}$padding"
    }
    
    <.>()(
      if (text.nonEmpty) Some(
        <.text(
          ^.rbWidth := text.length,
          ^.rbHeight := 1,
          ^.rbLeft := {
            props.align match {
              case Left => left
              case Center => left + (props.width - text.length) / 2
              case Right => left + props.width - text.length
            }
          },
          ^.rbTop := top,
          ^.rbStyle := {
            if (props.focused) props.style.focus.orNull
            else props.style
          },
          ^.content := text
        )()
      )
      else None
    )
  }
}
