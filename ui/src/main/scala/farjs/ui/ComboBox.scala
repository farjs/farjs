package farjs.ui

import farjs.ui.popup.PopupOverlay
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.raw.BlessedProgram
import scommons.react.hooks._

object ComboBox extends FunctionComponent[ComboBoxProps] {

  private[ui] var textInputComp: UiComponent[TextInputProps] = TextInput
  private[ui] var comboBoxPopup: UiComponent[ComboBoxPopupProps] = ComboBoxPopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val programRef = useRef[BlessedProgram](null)
    val (maybePopup, setPopup) = useState[Option[(List[String], Int)]](None)
    val (state, setState) = useStateUpdater(() => TextInputState())
    
    def showPopup(items: List[String], selected: Int): Unit = {
      setPopup(Some(items -> selected))
      if (programRef.current != null) {
        programRef.current.hideCursor()
      }
    }

    def hidePopup(): Unit = {
      setPopup(None)
      if (programRef.current != null) {
        programRef.current.showCursor()
      }
    }

    def onAction(items: List[String], selected: Int): Unit = {
      val value = items(selected)
      props.onChange(value)
      setState(_.copy(offset = 0, cursorX = value.length, selStart = -1, selEnd = -1))
      hidePopup()
    }
    
    def onKeypress(keyFull: String): Boolean = {
      var processed = true
      keyFull match {
        case "escape" | "tab" =>
          if (maybePopup.isDefined) hidePopup()
          else processed = false
        case "C-up" | "C-down" =>
          if (maybePopup.isDefined) hidePopup()
          else showPopup(List("item", "item 2"), 0)
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
        case _ => processed = maybePopup.isDefined
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
        <.form(
          ^.ref := { el: BlessedElement =>
            if (el != null) {
              programRef.current = el.screen.program
            }
          },
          ^.rbClickable := true,
          ^.rbMouse := true,
          ^.rbAutoFocus := false,
          ^.rbStyle := PopupOverlay.style,
          ^.rbOnClick := { _ =>
            hidePopup()
          }
        )(
          <(comboBoxPopup())(^.wrapped := ComboBoxPopupProps(
            selected = selected,
            items = items,
            left = props.left,
            top = props.top + 1,
            width = props.width,
            onClick = { index =>
              onAction(items, index)
            }
          ))()
        )
      }
    )
  }
}
