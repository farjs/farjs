package farjs.ui

import farjs.ui.popup.PopupOverlay
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.raw.BlessedProgram
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

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

    def onSelectAction(items: List[String], selected: Int): Unit = {
      if (items.nonEmpty) {
        props.onChange(items(selected))
        hidePopup()

        process.stdin.emit("keypress", js.undefined, js.Dynamic.literal(
          name = "end",
          ctrl = false,
          meta = false,
          shift = false
        ))
      }
    }

    def onAutoCompleteAction(key: String): Unit = {
      val value =
        if (state.selStart != -1) props.value.take(state.selStart)
        else props.value

      val newValue =
        if (key.length == 1) s"$value$key"
        else if (key.startsWith("S-") && key.length == 3) s"""$value${key.drop(2).toUpperCase}"""
        else if (key == "space") s"$value "
        else value

      if (newValue != value) {
        props.items.find(_.startsWith(newValue)).foreach { existing =>
          Future {
            props.onChange(existing)

            process.stdin.emit("keypress", js.undefined, js.Dynamic.literal(
              name = "end",
              ctrl = false,
              meta = false,
              shift = true
            ))
          }
        }
      }
    }
    
    def onKeypress(keyFull: String): Boolean = {
      var processed = true
      keyFull match {
        case "escape" | "tab" =>
          if (maybePopup.isDefined) hidePopup()
          else processed = false
        case "C-up" | "C-down" =>
          if (maybePopup.isDefined) hidePopup()
          else showPopup(props.items.toList, 0)
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
            case Some((items, selected)) => onSelectAction(items, selected)
          }
        case key if maybePopup.isEmpty =>
          onAutoCompleteAction(key)
          processed = false
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
              onSelectAction(items, index)
            }
          ))()
        )
      }
    )
  }
}