package farjs.ui

import scommons.react._
import scommons.react.blessed._

case class CheckBoxProps(pos: (Int, Int),
                         value: Boolean,
                         label: String,
                         style: BlessedStyle,
                         onChange: () => Unit)

object CheckBox extends FunctionComponent[CheckBoxProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (left, top) = props.pos
    
    <.>()(
      <.button(
        ^.rbMouse := true,
        ^.rbWidth := 3,
        ^.rbHeight := 1,
        ^.rbLeft := left,
        ^.rbTop := top,
        ^.rbStyle := props.style,
        ^.rbOnPress := props.onChange,
        ^.content := {
          if (props.value) "[x]"
          else "[ ]"
        }
      )(),
      
      <.text(
        ^.rbHeight := 1,
        ^.rbLeft := left + 4,
        ^.rbTop := top,
        ^.rbStyle := props.style,
        ^.content := props.label
      )()
    )
  }
}
