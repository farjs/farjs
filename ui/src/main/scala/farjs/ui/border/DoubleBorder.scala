package farjs.ui.border

import farjs.ui.{TextLine, TextLineProps}
import scommons.react._
import scommons.react.blessed._

case class DoubleBorderProps(size: (Int, Int),
                             style: BlessedStyle,
                             pos: (Int, Int) = (0, 0),
                             title: Option[String] = None)

object DoubleBorder extends FunctionComponent[DoubleBorderProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (width, height) = props.size
    val (left, top) = props.pos

    <.>()(
      <(HorizontalLine())(^.wrapped := HorizontalLineProps(
        pos = props.pos,
        length = width,
        lineCh = horizontalCh,
        style = props.style,
        startCh = Some(topLeftCh),
        endCh = Some(topRightCh)
      ))(),
      
      props.title.map { title =>
        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Center,
          pos = props.pos,
          width = width,
          text = title,
          style = props.style
        ))()
      },

      <(VerticalLine())(^.wrapped := VerticalLineProps(
        pos = (left, top + 1),
        length = height - 2,
        lineCh = verticalCh,
        style = props.style
      ))(),
      
      <(VerticalLine())(^.wrapped := VerticalLineProps(
        pos = (left + width - 1, top + 1),
        length = height - 2,
        lineCh = verticalCh,
        style = props.style
      ))(),

      <(HorizontalLine())(^.wrapped := HorizontalLineProps(
        pos = (left, top + height - 1),
        length = width,
        lineCh = horizontalCh,
        style = props.style,
        startCh = Some(bottomLeftCh),
        endCh = Some(bottomRightCh)
      ))()
    )
  }

  // lines
  val horizontalCh = "\u2550"
  val verticalCh = "\u2551"

  // corners
  val topLeftCh = "\u2554"
  val topRightCh = "\u2557"
  val bottomLeftCh = "\u255a"
  val bottomRightCh = "\u255d"
  
  // connectors
  val topCh = "\u2566"
  val bottomCh = "\u2569"
  val leftCh = "\u2560"
  val rightCh = "\u2563"
  
  // single connectors
  val topSingleCh = "\u2564"
  val bottomSingleCh = "\u2567"
  val leftSingleCh = "\u255f"
  val rightSingleCh = "\u2562"

  // crosses
  val crossCh = "\u256c"
  val crossSingleVertCh = "\u256a"
  val crossSingleHorizCh = "\u256b"
}
