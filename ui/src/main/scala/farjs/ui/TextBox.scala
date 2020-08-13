package farjs.ui

import scommons.react._
import scommons.react.blessed._
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

    useLayoutEffect({ () =>
      move(elementRef.current, props.value, CursorMove.End)
    }, Nil)

    def move(el: BlessedElement, value: String, cm: CursorMove): Unit = {
      val (posX, idx) = cm match {
        case CursorMove.At(pos) => (pos, offset)
        case CursorMove.Home => (0, 0)
        case CursorMove.End => (value.length, value.length - el.width + 1)
        case CursorMove.Left => (cursorX - 1, if (cursorX == 0) offset - 1 else offset)
        case CursorMove.Right => (cursorX + 1, if (cursorX == el.width - 1) offset + 1 else offset)
      }

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
        el.screen.program.omove(el.aleft + newPos, el.atop)
        setCursorX(newPos)
      }
    }
    
    def onClick(data: MouseData): Unit = {
      val el = elementRef.current
      val screen = el.screen
      move(el, props.value, CursorMove.At(data.x - el.aleft))
      if (screen.focused != el) {
        el.focus()
      }
    }
    
    val onResize: js.Function0[Unit] = { () =>
      val el = elementRef.current
      val screen = el.screen
      if (screen.focused == el) {
        screen.program.omove(el.aleft + cursorX, el.atop)
      }
    }
    
    val onFocus: js.Function0[Unit] = { () =>
      val el = elementRef.current
      val screen = el.screen
      val cursor = screen.cursor
      if (cursor.shape != "underline" || !cursor.blink) {
        screen.cursorShape("underline", blink = true)
      }
      
      screen.program.showCursor()
    }
    
    val onBlur: js.Function0[Unit] = { () =>
      val el = elementRef.current
      el.screen.program.hideCursor()
    }
    
    def onKeypress(ch: js.Dynamic, key: KeyboardKey): Unit = {
      val el = elementRef.current

      var processed = true
      key.full match {
        case "escape" | "return" | "enter" | "tab" =>
          processed = false
        case "right" => move(el, props.value, CursorMove.Right)
        case "left" => move(el, props.value, CursorMove.Left)
        case "home" => move(el, props.value, CursorMove.Home)
        case "end" => move(el, props.value, CursorMove.End)
        case "delete" =>
          edit(props.value, TextEdit.Delete)
        case "backspace" =>
          val newVal = edit(props.value, TextEdit.Backspace)
          if (props.value != newVal) {
            move(el, newVal, CursorMove.Left)
          }
        case _ =>
          if (ch != null && !js.isUndefined(ch)) {
            val newVal = edit(props.value, TextEdit.Insert(ch.toString))
            move(el, newVal, CursorMove.Right)
          }
          else processed = false
      }
      
      key.defaultPrevented = processed
    }
    
    def edit(value: String, te: TextEdit): String = {
      val newVal = te match {
        case TextEdit.Delete =>
          value.slice(0, offset + cursorX) + value.slice(offset + cursorX + 1, value.length)
        case TextEdit.Backspace =>
          value.slice(0, offset + cursorX - 1) + value.slice(offset + cursorX, value.length)
        case TextEdit.Insert(s) =>
          value.slice(0, offset + cursorX) + s + value.slice(offset + cursorX, value.length)
      }

      if (value != newVal) {
        props.onChange(newVal)
      }
      newVal
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
      ^.content := props.value.substring(offset),
      ^.rbOnClick := onClick,
      ^.rbOnResize := onResize,
      ^.rbOnFocus := onFocus,
      ^.rbOnBlur := onBlur,
      ^.rbOnKeypress := onKeypress
    )()
  }
  
  private sealed trait CursorMove
  
  private object CursorMove {
    
    case class At(pos: Int) extends CursorMove
    case object Home extends CursorMove
    case object End extends CursorMove
    case object Left extends CursorMove
    case object Right extends CursorMove
  }
  
  private sealed trait TextEdit
  
  private object TextEdit {
    
    case class Insert(str: String) extends TextEdit
    case object Delete extends TextEdit
    case object Backspace extends TextEdit
  }
}
