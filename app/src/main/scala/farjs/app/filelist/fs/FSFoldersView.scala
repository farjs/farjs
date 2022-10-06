package farjs.app.filelist.fs

import farjs.ui._
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

case class FSFoldersViewProps(left: Int,
                              top: Int,
                              width: Int,
                              height: Int,
                              selected: Int,
                              items: List[String],
                              style: BlessedStyle,
                              onAction: Int => Unit)

object FSFoldersView extends FunctionComponent[FSFoldersViewProps] {

  private[fs] var scrollBarComp: UiComponent[ScrollBarProps] = ScrollBar

  private def renderItems(selected: Int,
                          items: List[String],
                          width: Int,
                          theme: BlessedStyle): List[String] = {

    items.zipWithIndex.map {
      case (item, index) =>
        val style =
          if (selected == index) theme.focus.getOrElse(null)
          else theme

        val text = item
          .replace("\n", "")
          .replace("\r", "")
          .replace('\t', ' ')

        TextBox.renderText(
          isBold = style.bold.getOrElse(false),
          fgColor = style.fg.orNull,
          bgColor = style.bg.orNull,
          text = text.take(width).padTo(width, ' ')
        )
    }
  }

  protected def render(compProps: Props): ReactElement = {
    val elementRef = useRef[BlessedElement](null)
    val props = compProps.wrapped
    val (viewport@ListViewport(offset, focused, length, viewLength), setViewport) = useState(
      ListViewport(props.selected, props.items.size, props.height)
    )
    val itemsContent =
      renderItems(focused, props.items.slice(offset, offset + viewLength), props.width, props.style)
        .mkString(UI.newLine)

    val onKeypress: js.Function2[js.Dynamic, KeyboardKey, Unit] = { (_, key) =>
      key.full match {
        case "return" => props.onAction(offset + focused)
        case key => viewport.onKeypress(key).foreach(setViewport)
      }
    }
    
    useLayoutEffect({ () =>
      setViewport(viewport.resize(props.height))
    }, List(props.height))

    <.button(
      ^.rbLeft := props.left,
      ^.rbTop := props.top,
      ^.rbWidth := props.width,
      ^.rbHeight := props.height,
      ^.rbOnKeypress := onKeypress
    )(
      <.text(
        ^.reactRef := elementRef,
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbWidth := props.width,
        ^.rbHeight := props.height,
        ^.rbOnWheelup := { _ =>
          setViewport(viewport.up)
        },
        ^.rbOnWheeldown := { _ =>
          setViewport(viewport.down)
        },
        ^.rbOnClick := { data =>
          val curr = elementRef.current
          val y = data.y - curr.atop
          val index = offset + y
          if (index < length) {
            props.onAction(index)
          }
        },
        ^.rbStyle := props.style,
        ^.rbTags := true,
        ^.content := itemsContent
      )(),

      if (viewport.length > viewport.viewLength) Some {
        <(scrollBarComp())(^.plain := ScrollBarProps(
          left = props.left + props.width - 1,
          top = props.top - 1,
          length = viewport.viewLength,
          style = props.style,
          value = viewport.offset,
          extent = viewport.viewLength,
          min = 0,
          max = viewport.length - viewport.viewLength,
          onChange = { offset =>
            setViewport(viewport.copy(offset = offset))
          }
        ))()
      }
      else None
    )
  }
}
