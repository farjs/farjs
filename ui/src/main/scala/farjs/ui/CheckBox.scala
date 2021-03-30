package farjs.ui

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

case class CheckBoxProps(pos: (Int, Int),
                         value: Boolean,
                         label: String,
                         style: BlessedStyle,
                         onChange: () => Unit)

object CheckBox extends FunctionComponent[CheckBoxProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val (focused, setFocused) = useState(false)
    val props = compProps.wrapped
    val (left, top) = props.pos
    
    <.>()(
      <.button(
        ^.rbMouse := true,
        ^.rbTags := true,
        ^.rbWidth := 4,
        ^.rbHeight := 1,
        ^.rbLeft := left,
        ^.rbTop := top,
        ^.rbOnPress := props.onChange,
        ^.rbOnFocus := { () =>
          setFocused(true)
        },
        ^.rbOnBlur := { () =>
          setFocused(false)
        },
        ^.content := {
          val style =
            if (focused) props.style.focus.orNull
            else props.style
          
          TextBox.renderText(style, if (props.value) "[x]" else "[ ]")
        }
      )(),
      
      <.text(
        ^.rbHeight := 1,
        ^.rbLeft := left + 3,
        ^.rbTop := top,
        ^.rbStyle := props.style,
        ^.content := s" ${props.label}"
      )()
    )
  }
}
