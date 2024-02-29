package farjs.ui

import farjs.ui.ComboBoxPopup.maxItems
import farjs.ui.popup.PopupOverlay
import farjs.ui.theme.Theme
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.raw.BlessedProgram
import scommons.react.hooks._

import scala.scalajs.js

object ComboBox extends FunctionComponent[ComboBoxProps] {

  private[ui] var textInputComp: UiComponent[TextInputProps] = TextInput
  private[ui] var comboBoxPopup: UiComponent[ComboBoxPopupProps] = ComboBoxPopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val inputRef = useRef[BlessedElement](null)
    val programRef = useRef[BlessedProgram](null)
    val autoCompleteTimeoutRef = useRef[Timeout](null)
    val (maybePopup, setPopup) = useState[Option[ListViewport]](None)
    val (state, setState) = useStateUpdater(() => TextInputState())
    val currTheme = Theme.useTheme()
    val theme = currTheme.popup.menu
    val arrowStyle = currTheme.popup.regular
    
    def showOrHidePopup(): Unit = {
      if (maybePopup.isDefined) hidePopup()
      else {
        showPopup(ListViewport(0, props.items.length, maxItems))
      }
    }
    
    def showPopup(viewport: ListViewport): Unit = {
      setPopup(Some(viewport))

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

    def onSelectAction(offset: Int, index: Int): Unit = {
      if (props.items.length > 0) {
        props.onChange(props.items(offset + index))
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
        if (autoCompleteTimeoutRef.current != null) {
          global.clearTimeout(autoCompleteTimeoutRef.current)
          autoCompleteTimeoutRef.current = null
        }
        
        props.items.find(_.startsWith(newValue)).foreach { existing =>
          autoCompleteTimeoutRef.current = global.setTimeout(() => {
            props.onChange(existing)

            process.stdin.emit("keypress", js.undefined, js.Dynamic.literal(
              name = "end",
              ctrl = false,
              meta = false,
              shift = true
            ))
          }, 25)
        }
      }
    }
    
    def onKeypress(keyFull: String): Boolean = {
      var processed = maybePopup.isDefined
      keyFull match {
        case "escape" | "tab" => hidePopup()
        case "C-up" | "C-down" =>
          showOrHidePopup()
          processed = true
        case "return" =>
          maybePopup.foreach { viewport =>
            onSelectAction(viewport.offset, viewport.focused)
          }
        case key =>
          maybePopup match {
            case None => onAutoCompleteAction(key)
            case Some(viewport) =>
              viewport.onKeypress(key).foreach { newViewport =>
                setPopup(Some(newViewport))
              }
          }
      }
      processed
    }
    
    <.>()(
      <(textInputComp())(^.wrapped := TextInputProps(
        inputRef = inputRef,
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

      <.text(
        ^.rbWidth := 1,
        ^.rbHeight := 1,
        ^.rbLeft := props.left + props.width,
        ^.rbTop := props.top,
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := arrowStyle,
        ^.rbOnClick := { _ =>
          val el = inputRef.current
          if (el != null && el.screen.focused != el) {
            el.focus()
          }
          showOrHidePopup()
        },
        ^.content := arrowDownCh
      )(),

      maybePopup.map { viewport =>
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
          <(comboBoxPopup())(^.plain := ComboBoxPopupProps(
            left = props.left,
            top = props.top + 1,
            width = props.width,
            items = props.items,
            viewport = viewport,
            setViewport = { viewport =>
              setPopup(Some(viewport))
            },
            style = theme,
            onClick = { index =>
              onSelectAction(offset = 0, index)
            }
          ))()
        )
      }
    )
  }

  private[ui] val arrowDownCh = "\u2193" // ↓
}
