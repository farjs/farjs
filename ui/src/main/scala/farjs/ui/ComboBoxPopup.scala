package farjs.ui

import farjs.ui.border._
import scommons.react._
import scommons.react.blessed._

import scala.scalajs.js

case class ComboBoxPopupProps(selected: Int,
                              items: List[String],
                              left: Int,
                              top: Int,
                              width: Int,
                              style: BlessedStyle,
                              onClick: Int => Unit,
                              onWheel: Boolean => Unit = _ => ())

object ComboBoxPopup extends FunctionComponent[ComboBoxPopupProps] {
  
  private[ui] var singleBorderComp: UiComponent[SingleBorderProps] = SingleBorder

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val width = props.width
    val height = maxItems + 2
    val textWidth = width - 2
    val theme = props.style

    val onWheelup: js.Function1[MouseData, Unit] = { _ =>
      props.onWheel(true)
    }
    
    val onWheeldown: js.Function1[MouseData, Unit] = { _ =>
      props.onWheel(false)
    }
    
    <.box(
      ^.rbClickable := true,
      ^.rbAutoFocus := false,
      ^.rbWidth := width,
      ^.rbHeight := height,
      ^.rbLeft := props.left,
      ^.rbTop := props.top,
      ^.rbOnWheelup := onWheelup,
      ^.rbOnWheeldown := onWheeldown,
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
          ^.rbOnWheelup := onWheelup,
          ^.rbOnWheeldown := onWheeldown,
          ^.content := s"  ${text.take(textWidth - 4)}  "
        )()
      }
    )
  }

  private[ui] val maxItems = 8
}
