package farjs.ui

import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.raw.BlessedProgram
import scommons.react.hooks._

import scala.scalajs.js

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

    def moveCursor(program: BlessedProgram, aleft: Int, atop: Int, posX: Int, value: String): Unit = {
      val newPos = math.min(math.max(posX, 0), value.length)
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
        moveCursor(screen.program, aleft, data.y, data.x - aleft, props.value)
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
          case "escape" | "return" | "enter" | "tab" =>
            processed = false
          case "right" => moveCursor(program, aleft, atop, cursorX + 1, props.value)
          case "left" => moveCursor(program, aleft, atop, cursorX - 1, props.value)
          case "home" => moveCursor(program, aleft, atop, 0, props.value)
          case "end" => moveCursor(program, aleft, atop, props.value.length, props.value)
          case "delete" =>
            val value = props.value
            val newVal = value.slice(0, cursorX) + value.slice(cursorX + 1, value.length)
            if (value != newVal) {
              props.onChange(newVal)
            }
          case "backspace" =>
            val value = props.value
            val newVal = value.slice(0, cursorX - 1) + value.slice(cursorX, value.length)
            if (value != newVal) {
              props.onChange(newVal)
              moveCursor(program, aleft, atop, cursorX - 1, newVal)
            }
          case _ =>
            if (ch != null && !js.isUndefined(ch)) {
              val value = props.value
              val newVal = value.slice(0, cursorX) + ch + value.slice(cursorX, value.length)
              props.onChange(newVal)
              moveCursor(program, aleft, atop, cursorX + 1, newVal)
            }
            else processed = false
        }
        key.defaultPrevented = processed
      }
    )()
  }
}
