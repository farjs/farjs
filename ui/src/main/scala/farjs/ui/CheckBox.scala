package farjs.ui

import scommons.react._
import scommons.react.blessed._

object CheckBox extends FunctionComponent[CheckBoxProps] {

  private[ui] var buttonComp: ReactClass = Button
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    
    <.>()(
      <(buttonComp)(^.plain := ButtonProps(
        left = props.left,
        top = props.top,
        label = if (props.value) "[x]" else "[ ]",
        style = props.style,
        onPress = props.onChange
      ))(),
      
      <.text(
        ^.rbHeight := 1,
        ^.rbLeft := props.left + 4,
        ^.rbTop := props.top,
        ^.rbStyle := props.style,
        ^.content := props.label
      )()
    )
  }
}
