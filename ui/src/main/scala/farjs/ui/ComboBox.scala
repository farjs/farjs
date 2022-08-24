package farjs.ui

import scommons.react._
import scommons.react.hooks._

object ComboBox extends FunctionComponent[ComboBoxProps] {

  private[ui] var textInputComp: UiComponent[TextInputProps] = TextInput

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val (state, setState) = useStateUpdater(() => TextInputState())
    
    def onKeypress(keyFull: String): Boolean = keyFull match {
      case "C-up" | "C-down" =>
        //TODO: show ComboBoxPopup
        true
      case _ => false
    }
    
    <(textInputComp())(^.wrapped := TextInputProps(
      left = props.left,
      top = props.top,
      width = props.width,
      value = props.value,
      state = state,
      stateUpdater = setState,
      onChange = props.onChange,
      onEnter = props.onEnter,
      onKeypress = onKeypress
    ))()
  }
}
