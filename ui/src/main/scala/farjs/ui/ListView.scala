package farjs.ui

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

case class ListViewProps(left: Int,
                         top: Int,
                         width: Int,
                         height: Int,
                         items: js.Array[String],
                         viewport: ListViewport,
                         setViewport: js.Function1[ListViewport, Unit],
                         style: BlessedStyle,
                         onClick: js.Function1[Int, Unit])

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
    val viewport@ListViewport(offset, focused, length, viewLength) = props.viewport
    val itemsContent =
      renderItems(focused, props.items.slice(offset, offset + viewLength), props.width, props.style)
        .mkString(UI.newLine)

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
      ^.content := itemsContent
    )()
  }
}
