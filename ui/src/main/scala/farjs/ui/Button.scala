package farjs.ui

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

case class ButtonProps(pos: (Int, Int),
                       label: String,
                       style: BlessedStyle,
                       onPress: () => Unit)

object Button extends FunctionComponent[ButtonProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val (focused, setFocused) = useState(false)
    val props = compProps.wrapped
    val (left, top) = props.pos
    
    <.button(
      ^.rbMouse := true,
      ^.rbTags := true,
      ^.rbWidth := props.label.length,
      ^.rbHeight := 1,
      ^.rbLeft := left,
      ^.rbTop := top,
      ^.rbOnPress := props.onPress,
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
        
        TextBox.renderText(style, props.label)
      }
    )()
  }
}
