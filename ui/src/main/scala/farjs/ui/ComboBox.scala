package farjs.ui

import scommons.react._
import scommons.react.hooks._

object ComboBox extends FunctionComponent[ComboBoxProps] {

  private[ui] var textInputComp: UiComponent[TextInputProps] = TextInput
  private[ui] var comboBoxPopup: UiComponent[ComboBoxPopupProps] = ComboBoxPopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val (maybePopup, setPopup) = useState[Option[(List[String], Int)]](None)
    val (state, setState) = useStateUpdater(() => TextInputState())
    
    def onAction(items: List[String], selected: Int): Unit = {
      props.onChange(items(selected))
      setPopup(None)
    }
    
    def onKeypress(keyFull: String): Boolean = {
      var processed = true
      keyFull match {
        case "escape" =>
          if (maybePopup.isDefined) setPopup(None)
          else processed = false
        case "C-up" | "C-down" =>
          if (maybePopup.isDefined) setPopup(None)
          else setPopup(Some(List("item 1", "item 2") -> 0))
        case "down" =>
          maybePopup match {
            case None => processed = false
            case Some((items, selected)) =>
              if (selected < items.size - 1) {
                setPopup(Some((items, selected + 1)))
              }
          }
        case "up" =>
          maybePopup match {
            case None => processed = false
            case Some((items, selected)) =>
              if (selected > 0) {
                setPopup(Some((items, selected - 1)))
              }
          }
        case "return" =>
          maybePopup match {
            case None => processed = false
            case Some((items, selected)) => onAction(items, selected)
          }
        case _ => processed = false
      }
      processed
    }
    
    <.>()(
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
      ))(),

      maybePopup.map { case (items, selected) =>
        <(comboBoxPopup())(^.wrapped := ComboBoxPopupProps(
          selected = selected,
          items = items,
          top = props.top + 1,
          left = props.left,
          width = props.width,
          onClick = { index =>
            onAction(items, index)
          }
        ))()
      }
    )
  }
}
