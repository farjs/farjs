package farjs.ui

import scommons.react._
import scommons.react.blessed._

object TextLine extends FunctionComponent[TextLineProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val paddingSize = props.padding.getOrElse(1)
    val padding = " " * paddingSize
    val text = {
      val paddingLen = paddingSize * 2
      val dx = props.text.length - (props.width - paddingLen)
      if (dx > 0) {
        val prefix = props.text.take(3)
        val sufix = props.text.drop(dx + 6)
        s"$padding$prefix...$sufix$padding"
      }
      else s"$padding${props.text}$padding"
    }
    
    if (text.nonEmpty) {
      <.text(
        ^.rbWidth := text.length,
        ^.rbHeight := 1,
        ^.rbLeft := {
          props.align match {
            case TextAlign.left => props.left
            case TextAlign.right => props.left + props.width - text.length
            case _ => props.left + (props.width - text.length) / 2
          }
        },
        ^.rbTop := props.top,
        ^.rbStyle := {
          if (props.focused.getOrElse(false)) props.style.focus.orNull
          else props.style
        },
        ^.content := text
      )()
    }
    else null
  }
}
