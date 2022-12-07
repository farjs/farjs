package farjs.ui

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

case class ListBoxProps(left: Int,
                        top: Int,
                        width: Int,
                        height: Int,
                        selected: Int,
                        items: List[String],
                        style: BlessedStyle,
                        onAction: Int => Unit)

object ListBox extends FunctionComponent[ListBoxProps] {

  private[ui] var listViewComp: UiComponent[ListViewProps] = ListView
  private[ui] var scrollBarComp: UiComponent[ScrollBarProps] = ScrollBar

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (viewport, setViewport) =
      useState(ListViewport(props.selected, props.items.size, props.height))

    val onKeypress: js.Function2[js.Dynamic, KeyboardKey, Unit] = { (_, key) =>
      key.full match {
        case "return" => props.onAction(viewport.offset + viewport.focused)
        case key => viewport.onKeypress(key).foreach(setViewport)
      }
    }
    
    <.button(
      ^.rbLeft := props.left,
      ^.rbTop := props.top,
      ^.rbWidth := props.width,
      ^.rbHeight := props.height,
      ^.rbOnKeypress := onKeypress
    )(
      <(listViewComp())(^.wrapped := ListViewProps(
        left = 0,
        top = 0,
        width = props.width,
        height = props.height,
        items = props.items,
        viewport = viewport,
        setViewport = setViewport,
        style = props.style,
        onClick = props.onAction
      ))(),

      if (viewport.length > viewport.viewLength) Some {
        <(scrollBarComp())(^.plain := ScrollBarProps(
          left = props.width,
          top = 0,
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
