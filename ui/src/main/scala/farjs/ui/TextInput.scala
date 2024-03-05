package farjs.ui

import farjs.ui.UI.renderText2
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

case class TextInputProps(inputRef: ReactRef[BlessedElement],
                          left: Int,
                          top: Int,
                          width: Int,
                          value: String,
                          state: TextInputState,
                          stateUpdater: js.Function1[js.Function1[TextInputState, TextInputState], Unit],
                          onChange: js.Function1[String, Unit],
                          onEnter: js.UndefOr[js.Function0[Unit]],
                          onKeypress: String => Boolean = _ => false)

object TextInput extends FunctionComponent[TextInputProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val theme = Theme.useTheme().textBox
    val elementRef = props.inputRef
    val (offset, cursorX) = (props.state.offset, props.state.cursorX)
    val (selStart, selEnd) = (props.state.selStart, props.state.selEnd)

    useLayoutEffect({ () =>
      move(elementRef.current, props.value, CursorMove.End, TextSelect.All)
    }, Nil)

    def move(el: BlessedElement, value: String, cm: CursorMove, ts: TextSelect): Unit = {
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
      
      val newPos = math.min(
        math.max(posX, 0),
        math.min(
          math.max(el.width - 1, 0),
          math.max(value.length - newOffset, 0)
        )
      )
      if (newPos != cursorX) {
        el.screen.program.omove(el.aleft + newPos, el.atop)
      }
      
      select(value, offset + cursorX, newOffset + newPos, ts)
      props.stateUpdater(TextInputState.copy(_)(offset = newOffset, cursorX = newPos))
    }
    
    def select(value: String, idx: Int, newIdx: Int, ts: TextSelect): Unit = {
      val (newStart, newEnd) = ts match {
        case TextSelect.Reset => (-1, -1)
        case TextSelect.All => (0, value.length)
        case TextSelect.TillTheEnd => (if (selStart != -1) selStart else idx, value.length)
        case TextSelect.ToTheRight => (if (selStart != -1) selStart else idx, newIdx)
        case TextSelect.TillTheHome => (0, if (selEnd != -1) selEnd else idx)
        case TextSelect.ToTheLeft => (newIdx, if (selEnd != -1) selEnd else idx)
        case _ => (selStart, selEnd)
      }
      props.stateUpdater(TextInputState.copy(_)(selStart = newStart, selEnd = newEnd))
    }
    
    val onClick: js.Function1[MouseData, Unit] = { data =>
      val el = elementRef.current
      val screen = el.screen
      move(el, props.value, CursorMove.At(data.x - el.aleft), TextSelect.Reset)
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
    
    def edit(value: String, te: TextEdit): (String, CursorMove) = {
      val res@(newVal, _) = {
        if (selEnd - selStart > 0) {
          te match {
            case TextEdit.Delete | TextEdit.Backspace =>
              (value.slice(0, selStart) + value.slice(selEnd, value.length), CursorMove.At(selStart - offset))
            case TextEdit.Insert(s) =>
              (value.slice(0, selStart) + s + value.slice(selEnd, value.length), CursorMove.At(selStart + s.length - offset))
          }
        } else {
          val idx = offset + cursorX
          te match {
            case TextEdit.Delete =>
              (value.slice(0, idx) + value.slice(idx + 1, value.length), CursorMove.At(cursorX))
            case TextEdit.Backspace =>
              (value.slice(0, idx - 1) + value.slice(idx, value.length), CursorMove.Left)
            case TextEdit.Insert(s) =>
              (value.slice(0, idx) + s + value.slice(idx, value.length), CursorMove.Right)
          }
        }
      }

      if (value != newVal) {
        props.onChange(newVal)
      }
      res
    }

    val onKeypress: js.Function2[js.Dynamic, KeyboardKey, Unit] = { (ch, key) =>
      val el = elementRef.current

      var processed = true
      if (!props.onKeypress(key.full)) {
        key.full match {
          case "return" =>
            props.onEnter.foreach(_.apply())
            processed = true
          case "enter" => processed = true // either enter or return is handled, not both!
          case "escape" | "tab" => processed = false
          case "right"   => move(el, props.value, CursorMove.Right, TextSelect.Reset)
          case "S-right" => move(el, props.value, CursorMove.Right, TextSelect.ToTheRight)
          case "left"    => move(el, props.value, CursorMove.Left, TextSelect.Reset)
          case "S-left"  => move(el, props.value, CursorMove.Left, TextSelect.ToTheLeft)
          case "home"    => move(el, props.value, CursorMove.Home, TextSelect.Reset)
          case "S-home"  => move(el, props.value, CursorMove.Home, TextSelect.TillTheHome)
          case "end"     => move(el, props.value, CursorMove.End, TextSelect.Reset)
          case "S-end"   => move(el, props.value, CursorMove.End, TextSelect.TillTheEnd)
          case "C-a"     => move(el, props.value, CursorMove.End, TextSelect.All)
          case "C-c" =>
            if (selEnd - selStart > 0) {
              el.screen.copyToClipboard(props.value.slice(selStart, selEnd))
            }
          case "delete" =>
            val (newVal, curMove) = edit(props.value, TextEdit.Delete)
            if (props.value != newVal) {
              move(el, newVal, curMove, TextSelect.Reset)
            }
          case "backspace" =>
            val (newVal, curMove) = edit(props.value, TextEdit.Backspace)
            if (props.value != newVal) {
              move(el, newVal, curMove, TextSelect.Reset)
            }
          case _ =>
            if (ch != null && !js.isUndefined(ch)) {
              val (newVal, curMove) = edit(props.value, TextEdit.Insert(ch.toString))
              move(el, newVal, curMove, TextSelect.Reset)
            }
            else processed = false
        }
      }
      
      key.defaultPrevented = processed
    }
    
    <.input(
      ^.reactRef := elementRef,
      ^.rbAutoFocus := false,
      ^.rbClickable := true,
      ^.rbKeyable := true,
      ^.rbWidth := props.width,
      ^.rbHeight := 1,
      ^.rbLeft := props.left,
      ^.rbTop := props.top,
      ^.rbStyle := theme.regular,
      ^.rbTags := true,
      ^.content := {
        if (selEnd - selStart > 0) {
          val part1 = renderText2(theme.regular, props.value.slice(offset, selStart))
          val part2 = renderText2(theme.selected, props.value.slice(math.max(selStart, offset), selEnd))
          val part3 = renderText2(theme.regular, props.value.substring(math.min(selEnd, props.value.length)))
          s"$part1$part2$part3"
        } else renderText2(theme.regular, props.value.substring(math.min(offset, props.value.length)))
      },
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
  
  private sealed trait TextSelect
  
  private object TextSelect {
    
    case object Reset extends TextSelect
    case object All extends TextSelect
    case object TillTheHome extends TextSelect
    case object TillTheEnd extends TextSelect
    case object ToTheLeft extends TextSelect
    case object ToTheRight extends TextSelect
  }
}
