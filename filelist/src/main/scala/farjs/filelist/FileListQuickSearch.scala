package farjs.filelist

import farjs.ui.border._
import farjs.ui.popup.PopupOverlay
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

case class FileListQuickSearchProps(text: String, onClose: () => Unit)

object FileListQuickSearch extends FunctionComponent[FileListQuickSearchProps] {

  private[filelist] var doubleBorderComp: UiComponent[DoubleBorderProps] = DoubleBorder

  protected def render(compProps: Props): ReactElement = {
    val elementRef = useRef[BlessedElement](null)
    val props = compProps.wrapped
    val width = 25
    val height = 3
    val currTheme = Theme.useTheme
    val boxStyle = currTheme.popup.regular
    val textStyle = currTheme.textBox.regular
    val textWidth = width - 2
    val text = props.text.take(textWidth - 1)

    useLayoutEffect({ () =>
      val el = elementRef.current
      val screen = el.screen
      val cursor = screen.cursor
      if (cursor.shape != "underline" || !cursor.blink) {
        screen.cursorShape("underline", blink = true)
      }
      val program = screen.program
      program.showCursor()
      
      { () =>
        program.hideCursor()
      }
    }, Nil)

    val moveCursor: js.Function0[Unit] = { () =>
      val el = elementRef.current
      el.screen.program.omove(el.aleft + text.length, el.atop)
    }

    useLayoutEffect({ () =>
      moveCursor()
    }, List(text))
    
    <.form(
      ^.rbClickable := true,
      ^.rbMouse := true,
      ^.rbAutoFocus := false,
      ^.rbStyle := PopupOverlay.style,
      ^.rbOnResize := moveCursor,
      ^.rbOnClick := { _ =>
        props.onClose()
      }
    )(
      <.box(
        ^.rbClickable := true,
        ^.rbAutoFocus := false,
        ^.rbWidth := width,
        ^.rbHeight := height,
        ^.rbTop := "100%-3",
        ^.rbLeft := 10,
        ^.rbStyle := boxStyle
      )(
        <(doubleBorderComp())(^.plain := DoubleBorderProps(
          width = width,
          height = height,
          style = boxStyle,
          title = "Search"
        ))(),
  
        <.text(
          ^.reactRef := elementRef,
          ^.rbWidth := textWidth,
          ^.rbHeight := 1,
          ^.rbTop := 1,
          ^.rbLeft := 1,
          ^.rbStyle := textStyle,
          ^.content := text
        )()
      )
    )
  }
}
