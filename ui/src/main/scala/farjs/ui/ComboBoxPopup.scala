package farjs.ui

import farjs.ui.border._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._

case class ComboBoxPopupProps(selected: Int,
                              items: List[String],
                              top: Int,
                              left: Int,
                              width: Int,
                              onClick: Int => Unit)

object ComboBoxPopup extends FunctionComponent[ComboBoxPopupProps] {
  
  private[ui] var singleBorderComp: UiComponent[SingleBorderProps] = SingleBorder

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val width = props.width
    val height = maxItems + 2
    val textWidth = width - 2
    val theme = Theme.current.popup.menu

    <.box(
      ^.rbClickable := true,
      ^.rbAutoFocus := false,
      ^.rbWidth := width,
      ^.rbHeight := height,
      ^.rbTop := props.top,
      ^.rbLeft := props.left,
      ^.rbStyle := theme
    )(
      <(singleBorderComp())(^.plain := SingleBorderProps(
        width = width,
        height = height,
        style = theme
      ))(),

      props.items.zipWithIndex.map { case (text, index) =>
        <.text(
          ^.key := text,
          ^.rbHeight := 1,
          ^.rbWidth := textWidth,
          ^.rbLeft := 1,
          ^.rbTop := 1 + index,
          ^.rbClickable := true,
          ^.rbMouse := true,
          ^.rbAutoFocus := false,
          ^.rbStyle := {
            if (props.selected == index) theme.focus.getOrElse(null)
            else theme
          },
          ^.rbOnClick := { _ =>
            props.onClick(index)
          },
          ^.content := s"  ${text.take(textWidth - 4)}  "
        )()
      }
    )
  }

  private[ui] val maxItems = 8
}
