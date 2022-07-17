package farjs.ui

import scommons.react._
import scommons.react.blessed._

case class CheckBoxProps(pos: (Int, Int),
                         value: Boolean,
                         label: String,
                         style: BlessedStyle,
                         onChange: () => Unit)

object CheckBox extends FunctionComponent[CheckBoxProps] {

  private[ui] var buttonComp: UiComponent[ButtonProps] = Button
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (left, top) = props.pos
    
    <.>()(
      <(buttonComp())(^.plain := ButtonProps(
        left = left,
        top = top,
        label = if (props.value) "[x]" else "[ ]",
        style = props.style,
        onPress = props.onChange
      ))(),
      
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
