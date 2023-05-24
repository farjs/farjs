package farjs.ui

import scommons.react._
import scommons.react.blessed._

object TextLine extends FunctionComponent[TextLineProps] {
  
  def wrapText(text: String, width: Int, prefixLen: Int = 3): String = {
    val dx = text.length - width
    if (dx > 0) {
      val prefix = text.take(prefixLen)
      val sufix = text.drop(dx + prefixLen + 3) // prefix + ...
      s"$prefix...$sufix"
    }
    else text
  }
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val paddingSize = props.padding.getOrElse(1)
    val padding = " " * paddingSize
    val text = {
      val paddingLen = paddingSize * 2
      val wrapped = wrapText(props.text, props.width - paddingLen)
      s"$padding$wrapped$padding"
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
