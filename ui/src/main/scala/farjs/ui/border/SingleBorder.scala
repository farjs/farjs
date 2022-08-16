package farjs.ui.border

import scommons.react._
import scommons.react.blessed._

case class SingleBorderProps(size: (Int, Int),
                             style: BlessedStyle)

object SingleBorder extends FunctionComponent[SingleBorderProps] {

  private[border] var horizontalLineComp: UiComponent[HorizontalLineProps] = HorizontalLine
  private[border] var verticalLineComp: UiComponent[VerticalLineProps] = VerticalLine
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (width, height) = props.size

    <.>()(
      <(horizontalLineComp())(^.plain := HorizontalLineProps(
        left = 0,
        top = 0,
        length = width,
        lineCh = horizontalCh,
        style = props.style,
        startCh = topLeftCh,
        endCh = topRightCh
      ))(),
      
      <(verticalLineComp())(^.wrapped := VerticalLineProps(
        pos = (0, 1),
        length = height - 2,
        lineCh = verticalCh,
        style = props.style
      ))(),
      
      <(verticalLineComp())(^.wrapped := VerticalLineProps(
        pos = (width - 1, 1),
        length = height - 2,
        lineCh = verticalCh,
        style = props.style
      ))(),

      <(horizontalLineComp())(^.plain := HorizontalLineProps(
        left = 0,
        top = height - 1,
        length = width,
        lineCh = horizontalCh,
        style = props.style,
        startCh = bottomLeftCh,
        endCh = bottomRightCh
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
