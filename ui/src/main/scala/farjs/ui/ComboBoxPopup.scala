package farjs.ui

import farjs.ui.border._
import scommons.react._
import scommons.react.blessed._

import scala.scalajs.js

case class ComboBoxPopupProps(left: Int,
                              top: Int,
                              width: Int,
                              items: List[String],
                              viewport: ListViewport,
                              setViewport: js.Function1[ListViewport, Unit],
                              style: BlessedStyle,
                              onClick: Int => Unit)

object ComboBoxPopup extends FunctionComponent[ComboBoxPopupProps] {
  
  private[ui] var singleBorderComp: UiComponent[SingleBorderProps] = SingleBorder
  private[ui] var listViewComp: UiComponent[ListViewProps] = ListView

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val width = props.width
    val height = maxItems + 2
    val viewWidth = width - 2
    val theme = props.style

    <.box(
      ^.rbClickable := true,
      ^.rbAutoFocus := false,
      ^.rbWidth := width,
      ^.rbHeight := height,
      ^.rbLeft := props.left,
      ^.rbTop := props.top,
      ^.rbOnWheelup := { _ =>
        props.setViewport(props.viewport.up)
      },
      ^.rbOnWheeldown := { _ =>
        props.setViewport(props.viewport.down)
      },
      ^.rbStyle := theme
    )(
      <(singleBorderComp())(^.plain := SingleBorderProps(
        width = width,
        height = height,
        style = theme
      ))(),

      <(listViewComp())(^.wrapped := ListViewProps(
        left = 1,
        top = 1,
        width = viewWidth,
        height = height - 2,
        items = props.items.map(i => s"  ${i.take(viewWidth - 4)}  "),
        viewport = props.viewport,
        setViewport = props.setViewport,
        style = theme,
        onClick = props.onClick
      ))()
    )
  }

  private[ui] val maxItems = 8
}
