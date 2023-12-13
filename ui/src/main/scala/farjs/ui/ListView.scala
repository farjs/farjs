package farjs.ui

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

object ListView extends FunctionComponent[ListViewProps] {

  private def renderItems(selected: Int,
                          items: js.Array[String],
                          width: Int,
                          theme: BlessedStyle): js.Array[String] = {

    items.zipWithIndex.map {
      case (item, index) =>
        val style =
          if (selected == index) theme.focus.getOrElse(null)
          else theme

        val text = UiString(item
          .replace("\n", "")
          .replace("\r", "")
          .replace('\t', ' '))

        UI.renderText(
          isBold = style.bold.getOrElse(false),
          fgColor = style.fg.orNull,
          bgColor = style.bg.orNull,
          text = text.ensureWidth(width, ' ')
        )
    }
  }

  protected def render(compProps: Props): ReactElement = {
    val elementRef = useRef[BlessedElement](null)
    val props = compProps.plain
    val viewport@ListViewport(offset, focused, length, viewLength) = props.viewport
    val itemsContent =
      renderItems(focused, props.items.slice(offset, offset + viewLength), props.width, props.style)
        .mkString("\n")

    useLayoutEffect({ () =>
      props.setViewport(viewport.resize(props.height))
    }, List(props.height))

    <.text(
      ^.reactRef := elementRef,
      ^.rbClickable := true,
      ^.rbMouse := true,
      ^.rbAutoFocus := false,
      ^.rbLeft := props.left,
      ^.rbTop := props.top,
      ^.rbWidth := props.width,
      ^.rbHeight := props.height,
      ^.rbOnWheelup := { _ =>
        props.setViewport(viewport.up)
      },
      ^.rbOnWheeldown := { _ =>
        props.setViewport(viewport.down)
      },
      ^.rbOnClick := { data =>
        val curr = elementRef.current
        val y = data.y - curr.atop
        val index = offset + y
        if (index < length) {
          props.onClick(index)
        }
      },
      ^.rbStyle := props.style,
      ^.rbTags := true,
      ^.rbWrap := false,
      ^.content := itemsContent
    )()
  }
}
