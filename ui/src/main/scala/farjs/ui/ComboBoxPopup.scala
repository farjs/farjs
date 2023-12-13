package farjs.ui

import farjs.ui.border._
import scommons.react._
import scommons.react.blessed._

import scala.scalajs.js

case class ComboBoxPopupProps(left: Int,
                              top: Int,
                              width: Int,
                              items: js.Array[String],
                              viewport: ListViewport,
                              setViewport: js.Function1[ListViewport, Unit],
                              style: BlessedStyle,
                              onClick: js.Function1[Int, Unit])

object ComboBoxPopup extends FunctionComponent[ComboBoxPopupProps] {
  
  private[ui] var singleBorderComp: ReactClass = SingleBorder
  private[ui] var listViewComp: UiComponent[ListViewProps] = ListView
  private[ui] var scrollBarComp: UiComponent[ScrollBarProps] = ScrollBar

  private[ui] val maxItems = 8

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val width = props.width
    val height = maxItems + 2
    val viewWidth = width - 2
    val theme = props.style
    val viewport = props.viewport

    <.box(
      ^.rbClickable := true,
      ^.rbAutoFocus := false,
      ^.rbWidth := width,
      ^.rbHeight := height,
      ^.rbLeft := props.left,
      ^.rbTop := props.top,
      ^.rbOnWheelup := { _ =>
        props.setViewport(viewport.up)
      },
      ^.rbOnWheeldown := { _ =>
        props.setViewport(viewport.down)
      },
      ^.rbStyle := theme
    )(
      <(singleBorderComp)(^.plain := SingleBorderProps(
        width = width,
        height = height,
        style = theme
      ))(),

      <(listViewComp())(^.plain := ListViewProps(
        left = 1,
        top = 1,
        width = viewWidth,
        height = height - 2,
        items = props.items.map(i => s"  ${i.take(viewWidth - 4)}  "),
        viewport = viewport,
        setViewport = props.setViewport,
        style = theme,
        onClick = props.onClick
      ))(),

      if (viewport.length > viewport.viewLength) Some {
        <(scrollBarComp())(^.plain := ScrollBarProps(
          left = width - 1,
          top = 1,
          length = viewport.viewLength,
          style = theme,
          value = viewport.offset,
          extent = viewport.viewLength,
          min = 0,
          max = viewport.length - viewport.viewLength,
          onChange = { offset =>
            props.setViewport(viewport.copy(offset = offset))
          }
        ))()
      }
      else None
    )
  }
}
