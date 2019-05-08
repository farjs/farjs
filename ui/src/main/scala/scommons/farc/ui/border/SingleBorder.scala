package scommons.farc.ui.border

import scommons.react._
import scommons.react.blessed._

case class SingleBorderProps(size: (Int, Int),
                             style: BlessedStyle)

object SingleBorder extends FunctionComponent[SingleBorderProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (width, height) = props.size

    <.>()(
      <(HorizontalLine())(^.key := "0", ^.wrapped := HorizontalLineProps(
        pos = (0, 0),
        length = width,
        lineCh = horizontalCh,
        style = props.style,
        startCh = Some(topLeftCh),
        endCh = Some(topRightCh)
      ))(),
      
      <(VerticalLine())(^.key := "1", ^.wrapped := VerticalLineProps(
        pos = (0, 1),
        length = height - 2,
        lineCh = verticalCh,
        style = props.style
      ))(),
      
      <(VerticalLine())(^.key := "2", ^.wrapped := VerticalLineProps(
        pos = (width - 1, 1),
        length = height - 2,
        lineCh = verticalCh,
        style = props.style
      ))(),

      <(HorizontalLine())(^.key := "3", ^.wrapped := HorizontalLineProps(
        pos = (0, height - 1),
        length = width,
        lineCh = horizontalCh,
        style = props.style,
        startCh = Some(bottomLeftCh),
        endCh = Some(bottomRightCh)
      ))()
    )
  }

  // lines
  val horizontalCh = "\u2500"
  val verticalCh = "\u2502"
  
  // corners
  val topLeftCh = "\u250c"
  val topRightCh = "\u2510"
  val bottomLeftCh = "\u2514"
  val bottomRightCh = "\u2518"

  // connectors
  val topCh = "\u252c"
  val bottomCh = "\u2534"
  val leftCh = "\u251c"
  val rightCh = "\u2524"

  // double connectors
  val topDoubleCh = "\u2565"
  val bottomDoubleCh = "\u2568"
  val leftDoubleCh = "\u255e"
  val rightDoubleCh = "\u2561"

  // crosses
  val crossCh = "\u253c"
  val crossDoubleVertCh = "\u256b"
  val crossDoubleHorizCh = "\u256a"
}
