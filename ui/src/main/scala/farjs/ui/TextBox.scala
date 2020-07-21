package farjs.ui

import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.raw.BlessedProgram
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

    def moveCursor(program: BlessedProgram, aleft: Int, atop: Int, posX: Int): Unit = {
      val newPos = math.min(math.max(posX, 0), props.value.length)
      if (newPos != cursorX) {
        program.omove(aleft + newPos, atop)
        setCursorX(newPos)
      }
    }

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
        moveCursor(screen.program, aleft, data.y, data.x - aleft)
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
        val el = elementRef.current
        val program = el.screen.program
        val aleft = el.aleft
        val atop = el.atop
        
        var processed = true
        key.full match {
          case "right" => moveCursor(program, aleft, atop, cursorX + 1)
          case "left" => moveCursor(program, aleft, atop, cursorX - 1)
          case "home" => moveCursor(program, aleft, atop, 0)
          case "end" => moveCursor(program, aleft, atop, props.value.length)
          case _ =>
            processed = false
        }
        key.defaultPrevented = processed
      }
    )()
  }
}
