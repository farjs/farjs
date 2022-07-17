package farjs.ui

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

object Button extends FunctionComponent[ButtonProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val (focused, setFocused) = useState(false)
    val props = compProps.plain
    
    <.button(
      ^.rbMouse := true,
      ^.rbTags := true,
      ^.rbWrap := false,
      ^.rbWidth := props.label.length,
      ^.rbHeight := 1,
      ^.rbLeft := props.left,
      ^.rbTop := props.top,
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
