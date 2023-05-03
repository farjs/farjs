package farjs.ui

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

object TextBox extends FunctionComponent[TextBoxProps] {

  private[ui] var textInputComp: UiComponent[TextInputProps] = TextInput

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val (state, setState) = useStateUpdater(() => TextInputState())
    val inputRef = useRef[BlessedElement](null)
    
    <(textInputComp())(^.wrapped := TextInputProps(
      inputRef = inputRef,
      left = props.left,
      top = props.top,
      width = props.width,
      value = props.value,
      state = state,
      stateUpdater = setState,
      onChange = props.onChange,
      onEnter = props.onEnter
    ))()
  }
}
