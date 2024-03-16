package farjs.ui

import farjs.ui.UI.renderText2
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._
import scommons.react.raw.NativeRef

import scala.scalajs.js

object TextInput extends FunctionComponent[TextInputProps] {

  private def deref(ref: NativeRef): BlessedElement =
    ref.current.asInstanceOf[BlessedElement]

  private def isHighSurrogate(point: Int): Boolean =
    point >= 0xD800 && point <= 0xDBFF 
  
  private def isLowSurrogate(point: Int): Boolean =
    point >= 0xDC00 && point <= 0xDFFF 
  
  protected def render(compProps: Props): ReactElement = {
    val insertHighSurrogate = useRef("")
    val props = compProps.plain
    val theme = Theme.useTheme().textBox
    val elementRef = props.inputRef
    val (offset, cursorX) = (props.state.offset, props.state.cursorX)
    val (selStart, selEnd) = (props.state.selStart, props.state.selEnd)
    val currValue = UiString(props.value)

    useLayoutEffect({ () =>
      move(deref(elementRef), currValue, CursorMove.End, TextSelect.All)
    }, Nil)

    def move(el: BlessedElement, value: UiString, cm: CursorMove, ts: TextSelect): Unit = {
      val charStart = value.charStartPos(offset + cursorX)
      val (posX, idx) = cm match {
        case CursorMove.At(pos) => (pos, offset)
        case CursorMove.Home => (0, 0)
        case CursorMove.End => (value.strWidth(), value.strWidth() - el.width + 1)
        case CursorMove.Left(maybeDx) =>
          val dx = maybeDx.getOrElse(math.max(charStart.lcw, 1))
          (cursorX - dx, if (cursorX == 0) offset - dx else offset)
        case CursorMove.Right(maybeDx) =>
          val dx = maybeDx.getOrElse(math.max(charStart.rcw, 1))
          (cursorX + dx, if (cursorX == el.width - 1) offset + dx else offset)
      }

      val newOffset = math.min(
        math.max(idx, 0),
        value.strWidth()
      )
      
      val newPos = math.min(
        math.max(posX, 0),
        math.min(
          math.max(el.width - 1, 0),
          math.max(value.strWidth() - newOffset, 0)
        )
      )
      if (newPos != cursorX) {
        el.screen.program.omove(el.aleft + newPos, el.atop)
      }
      
      select(value, charStart.pos, newOffset + newPos, ts)
      props.stateUpdater(TextInputState.copy(_)(offset = newOffset, cursorX = newPos))
    }
    
    def select(value: UiString, idx: Int, newIdx: Int, ts: TextSelect): Unit = {
      val (newStart, newEnd) = ts match {
        case TextSelect.Reset => (-1, -1)
        case TextSelect.All => (0, value.strWidth())
        case TextSelect.TillTheEnd => (if (selStart != -1) selStart else idx, value.strWidth())
        case TextSelect.ToTheRight => (if (selStart != -1) selStart else idx, newIdx)
        case TextSelect.TillTheHome => (0, if (selEnd != -1) selEnd else idx)
        case TextSelect.ToTheLeft => (newIdx, if (selEnd != -1) selEnd else idx)
        case _ => (selStart, selEnd)
      }
      props.stateUpdater(TextInputState.copy(_)(selStart = newStart, selEnd = newEnd))
    }
    
    val onClick: js.Function1[MouseData, Unit] = { data =>
      val el = deref(elementRef)
      val screen = el.screen
      move(el, currValue, CursorMove.At(data.x - el.aleft), TextSelect.Reset)
      if (screen.focused != el) {
        el.focus()
      }
    }
    
    val onResize: js.Function0[Unit] = { () =>
      val el = deref(elementRef)
      val screen = el.screen
      if (screen.focused == el) {
        screen.program.omove(el.aleft + cursorX, el.atop)
      }
    }
    
    val onFocus: js.Function0[Unit] = { () =>
      val el = deref(elementRef)
      val screen = el.screen
      val cursor = screen.cursor
      if (cursor.shape != "underline" || !cursor.blink) {
        screen.cursorShape("underline", blink = true)
      }
      
      screen.program.showCursor()
    }
    
    val onBlur: js.Function0[Unit] = { () =>
      val el = deref(elementRef)
      el.screen.program.hideCursor()
    }
    
    def edit(value: UiString, te: TextEdit): (String, CursorMove) = {
      val res@(newVal, _) = {
        if (selEnd - selStart > 0) {
          te match {
            case TextEdit.Delete | TextEdit.Backspace =>
              (value.slice(0, selStart) + value.slice(selEnd, value.strWidth()), CursorMove.At(selStart - offset))
            case TextEdit.Insert(s) =>
              (value.slice(0, selStart) + s + value.slice(selEnd, value.strWidth()), CursorMove.At(selStart + s.strWidth() - offset))
          }
        } else {
          val idx = offset + cursorX
          te match {
            case TextEdit.Delete =>
              val charStart = value.charStartPos(idx)
              (value.slice(0, idx) + value.slice(idx + charStart.rcw, value.strWidth()), CursorMove.At(cursorX))
            case TextEdit.Backspace =>
              val charStart = value.charStartPos(idx)
              (value.slice(0, idx - 1) + value.slice(idx, value.strWidth()), CursorMove.Left(Some(charStart.lcw)))
            case TextEdit.Insert(s) =>
              (value.slice(0, idx) + s + value.slice(idx, value.strWidth()), CursorMove.Right(Some(s.strWidth())))
          }
        }
      }

      if (value.toString != newVal) {
        props.onChange(newVal)
      }
      res
    }

    val onKeypress: js.Function2[js.Dynamic, KeyboardKey, Unit] = { (ch, key) =>
      val el = deref(elementRef)

      def delete(te: TextEdit): Unit = {
        val (newVal, curMove) = edit(currValue, te)
        if (currValue.toString != newVal) {
          move(el, UiString(newVal), curMove, TextSelect.Reset)
        }
      }
      
      var processed = true
      if (!props.onKeypress.exists(_.apply(key.full))) {
        key.full match {
          case "return" =>
            props.onEnter.foreach(_.apply())
            processed = true
          case "enter" => processed = true // either enter or return is handled, not both!
          case "escape" | "tab" => processed = false
          case "right"   => move(el, currValue, CursorMove.Right(None), TextSelect.Reset)
          case "S-right" => move(el, currValue, CursorMove.Right(None), TextSelect.ToTheRight)
          case "left"    => move(el, currValue, CursorMove.Left(None), TextSelect.Reset)
          case "S-left"  => move(el, currValue, CursorMove.Left(None), TextSelect.ToTheLeft)
          case "home"    => move(el, currValue, CursorMove.Home, TextSelect.Reset)
          case "S-home"  => move(el, currValue, CursorMove.Home, TextSelect.TillTheHome)
          case "end"     => move(el, currValue, CursorMove.End, TextSelect.Reset)
          case "S-end"   => move(el, currValue, CursorMove.End, TextSelect.TillTheEnd)
          case "C-a"     => move(el, currValue, CursorMove.End, TextSelect.All)
          case "C-c" | "C-x" =>
            if (selEnd - selStart > 0) {
              el.screen.copyToClipboard(currValue.slice(selStart, selEnd))
              if (key.full == "C-x") {
                delete(TextEdit.Delete)
              }
            }
          case "delete" => delete(TextEdit.Delete)
          case "backspace" => delete(TextEdit.Backspace)
          case _ =>
            if (ch != null && !js.isUndefined(ch)) {
              val insertChar = ch.toString
              val code = insertChar.asInstanceOf[js.Dynamic].charCodeAt(0).asInstanceOf[Int]
              if (isHighSurrogate(code)) insertHighSurrogate.current = insertChar
              else {
                val combinedInsert =
                  if (isLowSurrogate(code)) insertHighSurrogate.current + insertChar
                  else insertChar

                insertHighSurrogate.current = ""
                val (newVal, curMove) = edit(currValue, TextEdit.Insert(UiString(combinedInsert)))
                move(el, UiString(newVal), curMove, TextSelect.Reset)
              }
            }
            else processed = false
        }
      }
      
      key.defaultPrevented = processed
    }
    
    <.input(
      ^.ref := { ref: BlessedElement =>
        elementRef.current = ref
      },
      ^.rbAutoFocus := false,
      ^.rbClickable := true,
      ^.rbKeyable := true,
      ^.rbWidth := props.width,
      ^.rbHeight := 1,
      ^.rbLeft := props.left,
      ^.rbTop := props.top,
      ^.rbStyle := theme.regular,
      ^.rbWrap := false,
      ^.rbTags := true,
      ^.content := {
        if (selEnd - selStart > 0) {
          val part1 = renderText2(theme.regular, currValue.slice(offset, selStart))
          val part2 = renderText2(theme.selected, currValue.slice(math.max(selStart, offset), selEnd))
          val part3 = renderText2(theme.regular, currValue.slice(selEnd, currValue.strWidth()))
          s"$part1$part2$part3"
        } else renderText2(theme.regular, currValue.slice(offset, currValue.strWidth()))
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
    case class Left(dx: Option[Int]) extends CursorMove
    case class Right(dx: Option[Int]) extends CursorMove
  }
  
  private sealed trait TextEdit
  
  private object TextEdit {
    
    case class Insert(str: UiString) extends TextEdit
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
