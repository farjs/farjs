package farjs.ui

import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.raw.Blessed
import scommons.react.hooks._

import scala.scalajs.js

case class TextBoxProps(pos: (Int, Int),
                        width: Int,
                        value: String,
                        onChange: String => Unit,
                        onEnter: () => Unit = () => ())

object TextBox extends FunctionComponent[TextBoxProps] {

  def renderText(style: BlessedStyle, text: String): String = {
    renderText(
      isBold = style.bold.getOrElse(false),
      fgColor = style.fg.getOrElse("white"),
      bgColor = style.bg.getOrElse("black"),
      text = text
    )
  }

  def renderText(isBold: Boolean, fgColor: String, bgColor: String, text: String): String = {
    if (text.isEmpty) text
    else {
      val bold = if (isBold) "{bold}" else ""
      s"$bold{$fgColor-fg}{$bgColor-bg}${Blessed.escape(text)}{/}"
    }
  }
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val elementRef = useRef[BlessedElement](null)
    val ((offset, cursorX), setOffsetAndPos) = useState(() => (0, -1))
    val ((selStart, selEnd), setSelStartAndEnd) = useState(() => (-1, -1))
    val (left, top) = props.pos

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
      setOffsetAndPos((newOffset, newPos))
    }
    
    def select(value: String, idx: Int, newIdx: Int, ts: TextSelect): Unit = {
      val selStartAndEnd = ts match {
        case TextSelect.Reset => (-1, -1)
        case TextSelect.All => (0, value.length)
        case TextSelect.TillTheEnd => (if (selStart != -1) selStart else idx, value.length)
        case TextSelect.ToTheRight => (if (selStart != -1) selStart else idx, newIdx)
        case TextSelect.TillTheHome => (0, if (selEnd != -1) selEnd else idx)
        case TextSelect.ToTheLeft => (newIdx, if (selEnd != -1) selEnd else idx)
        case _ => (selStart, selEnd)
      }
      setSelStartAndEnd(selStartAndEnd)
    }
    
    def onClick(data: MouseData): Unit = {
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
    
    def onKeypress(ch: js.Dynamic, key: KeyboardKey): Unit = {
      val el = elementRef.current

      var processed = true
      key.full match {
        case "return" =>
          props.onEnter()
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
      
      key.defaultPrevented = processed
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

    <.input(
      ^.reactRef := elementRef,
      ^.rbAutoFocus := false,
      ^.rbClickable := true,
      ^.rbKeyable := true,
      ^.rbWidth := props.width,
      ^.rbHeight := 1,
      ^.rbLeft := left,
      ^.rbTop := top,
      ^.rbStyle := styles.normal,
      ^.rbTags := true,
      ^.content := {
        if (selEnd - selStart > 0) {
          val part1 = renderText(styles.normal, props.value.slice(offset, selStart))
          val part2 = renderText(styles.selected, props.value.slice(math.max(selStart, offset), selEnd))
          val part3 = renderText(styles.normal, props.value.substring(math.min(selEnd, props.value.length)))
          s"$part1$part2$part3"
        } else renderText(styles.normal, props.value.substring(math.min(offset, props.value.length)))
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

  private[ui] object styles {

    val normal: BlessedStyle = new BlessedStyle {
      override val bold = false
      override val bg = "cyan"
      override val fg = "black"
    }
    val selected: BlessedStyle = new BlessedStyle {
      override val bold = false
      override val bg = "blue"
      override val fg = "white"
    }
  }
}
