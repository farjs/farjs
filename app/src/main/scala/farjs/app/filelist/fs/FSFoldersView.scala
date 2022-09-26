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
                              onAction: String => Unit)

object FSFoldersView extends FunctionComponent[FSFoldersViewProps] {

  private def renderItems(selected: Int,
                          items: List[String],
                          width: Int,
                          theme: BlessedStyle): List[String] = {
    val paddingLeft = "  "
    val textWidth = width - paddingLeft.length

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
          text = paddingLeft + text.take(textWidth).padTo(textWidth, ' ')
        )
    }
  }

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (viewport@ListViewport(offset, focused, _, viewLength), setViewport) = useState(
      ListViewport(offset = 0, props.selected, props.items.size, props.height)
    )
    val itemsContent =
      renderItems(focused, props.items.slice(offset, offset + viewLength), props.width, props.style)
        .mkString(UI.newLine)

    val onKeypress: js.Function2[js.Dynamic, KeyboardKey, Unit] = { (_, key) =>
      key.full match {
        case key => viewport.onKeypress(key).foreach(setViewport)
      }
    }

    <.button(
      ^.rbMouse := true,
      ^.rbLeft := props.left,
      ^.rbTop := props.top,
      ^.rbWidth := props.width,
      ^.rbHeight := props.height,
      ^.rbOnKeypress := onKeypress
    )(
      <.text(
        ^.rbWidth := props.width,
        ^.rbHeight := props.height,
        ^.rbStyle := props.style,
        ^.rbTags := true,
        ^.content := itemsContent
      )()
    )
  }
}
