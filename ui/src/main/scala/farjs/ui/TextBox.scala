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
    val (offset, setOffset) = useState(0)
    val (cursorX, setCursorX) = useState(-1)
    val (left, top) = props.pos

    def moveCursor(program: BlessedProgram, el: BlessedElement, posX: Int, value: String, idx: Int): Unit = {
      val newOffset = math.min(
        math.max(idx, 0),
        value.length
      )
      setOffset(newOffset)
      
      val newPos = math.min(
        math.max(posX, 0),
        math.min(
          math.max(el.width - 1, 0),
          math.max(value.length - newOffset, 0)
        )
      )
      if (newPos != cursorX) {
        program.omove(el.aleft + newPos, el.atop)
        setCursorX(newPos)
      }
    }
    
    useLayoutEffect({ () =>
      val el = elementRef.current
      moveCursor(el.screen.program, el, props.value.length, props.value, props.value.length - el.width + 1)
    }, Nil)

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
      ^.content := props.value.substring(offset),
      ^.rbOnClick := { data =>
        val el = elementRef.current
        val screen = el.screen
        moveCursor(screen.program, el, data.x - el.aleft, props.value, offset)
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
        screen.program.showCursor()
      },
      ^.rbOnBlur := { () =>
        elementRef.current.screen.program.hideCursor()
      },
      ^.rbOnKeypress := { (ch, key) =>
        val el = elementRef.current
        val program = el.screen.program
        
        var processed = true
        key.full match {
          case "escape" | "return" | "enter" | "tab" =>
            processed = false
          case "right" => moveCursor(program, el, cursorX + 1, props.value, if (cursorX == el.width - 1) offset + 1 else offset)
          case "left" => moveCursor(program, el, cursorX - 1, props.value, if (cursorX == 0) offset - 1 else offset)
          case "home" => moveCursor(program, el, 0, props.value, 0)
          case "end" => moveCursor(program, el, props.value.length, props.value, props.value.length - el.width + 1)
          case "delete" =>
            val value = props.value
            val newVal = value.slice(0, offset + cursorX) + value.slice(offset + cursorX + 1, value.length)
            if (value != newVal) {
              props.onChange(newVal)
            }
          case "backspace" =>
            val value = props.value
            val newVal = value.slice(0, offset + cursorX - 1) + value.slice(offset + cursorX, value.length)
            if (value != newVal) {
              props.onChange(newVal)
              moveCursor(program, el, cursorX - 1, newVal, if (cursorX == 0) offset - 1 else offset)
            }
          case _ =>
            if (ch != null && !js.isUndefined(ch)) {
              val value = props.value
              val newVal = value.slice(0, offset + cursorX) + ch + value.slice(offset + cursorX, value.length)
              props.onChange(newVal)
              moveCursor(program, el, cursorX + 1, newVal, if (cursorX == el.width - 1) offset + 1 else offset)
            }
            else processed = false
        }
        key.defaultPrevented = processed
      }
    )()
  }
}
