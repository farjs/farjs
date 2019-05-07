package scommons.farc.ui.border

import scommons.react._
import scommons.react.blessed._

case class DoubleBorderProps(size: (Int, Int),
                             style: BlessedStyle)

object DoubleBorder extends FunctionComponent[DoubleBorderProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (width, height) = props.size

    <.>()(
      <(HorizontalLine())(^.wrapped := HorizontalLineProps(
        pos = (0, 0),
        length = width,
        ch = horizontalCh,
        style = props.style,
        start = Some(topLeftCh),
        end = Some(topRightCh)
      ))(),
      
      <(VerticalLine())(^.wrapped := VerticalLineProps(
        pos = (0, 1),
        length = height - 2,
        ch = verticalCh,
        style = props.style
      ))(),
      
      <(VerticalLine())(^.wrapped := VerticalLineProps(
        pos = (width - 1, 1),
        length = height - 2,
        ch = verticalCh,
        style = props.style
      ))(),

      <(HorizontalLine())(^.wrapped := HorizontalLineProps(
        pos = (0, height - 1),
        length = width,
        ch = horizontalCh,
        style = props.style,
        start = Some(bottomLeftCh),
        end = Some(bottomRightCh)
      ))()
    )
  }

  val horizontalCh = "\u2550"
  val verticalCh = "\u2551"
  val topLeftCh = "\u2554"
  val topRightCh = "\u2557"
  val bottomLeftCh = "\u255a"
  val bottomRightCh = "\u255d"
}
