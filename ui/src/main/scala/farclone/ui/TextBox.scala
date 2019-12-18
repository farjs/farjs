package farclone.ui

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

case class TextBoxProps(pos: (Int, Int),
                        width: Int,
                        value: String,
                        style: BlessedStyle,
                        onChange: String => Unit)

object TextBox extends FunctionComponent[TextBoxProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val elementRef = useRef[BlessedElement](null)
    val (cursorX, setCursorX) = useState(props.value.length)
    val (left, top) = props.pos

    <.input(
      ^.reactRef := elementRef,
      ^.rbAutoFocus := false,
      ^.rbClickable := true,
      ^.rbKeyable := true,
      ^.rbWidth := props.width,
      ^.rbHeight := 1,
      ^.rbLeft := left,
      ^.rbTop := top,
      ^.rbStyle := props.style,
      ^.content := props.value,
      ^.rbOnClick := { data =>
        val el = elementRef.current
        val screen = el.screen
        val aleft = el.aleft
        val newPos = math.min(math.max(data.x - aleft, 0), props.value.length)
        if (newPos != cursorX) {
          screen.program.omove(aleft + newPos, data.y)
          setCursorX(newPos)
        }
        if (screen.focused != el) {
          el.focus()
        }
      },
      ^.rbOnResize := { () =>
        val el = elementRef.current
        val screen = el.screen
        if (screen.focused == el) {
          screen.program.omove(el.aleft + cursorX, el.atop)
        }
      },
      ^.rbOnFocus := { () =>
        val el = elementRef.current
        val screen = el.screen
        val cursor = screen.cursor
        if (cursor.shape != "underline" || !cursor.blink) {
          screen.cursorShape("underline", blink = true)
        }
        val program = screen.program
        program.omove(el.aleft + cursorX, el.atop)
        program.showCursor()
      },
      ^.rbOnBlur := { () =>
        elementRef.current.screen.program.hideCursor()
      },
      ^.rbOnKeypress := { (ch, key) =>
        if (key.full == "right" || key.full == "left") {
          key.defaultPrevented = true
        }
      }
    )()
  }
}
