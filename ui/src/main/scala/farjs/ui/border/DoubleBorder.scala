package farjs.ui.border

import farjs.ui.{TextAlign, TextLine, TextLineProps}
import scommons.react._

object DoubleBorder extends FunctionComponent[DoubleBorderProps] {

  private[border] var horizontalLineComp: UiComponent[HorizontalLineProps] = HorizontalLine
  private[border] var verticalLineComp: UiComponent[VerticalLineProps] = VerticalLine
  private[border] var textLineComp: UiComponent[TextLineProps] = TextLine

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val left = props.left.getOrElse(0)
    val top = props.top.getOrElse(0)

    <.>()(
      <(horizontalLineComp())(^.plain := HorizontalLineProps(
        left = left,
        top = top,
        length = props.width,
        lineCh = DoubleChars.horizontal,
        style = props.style,
        startCh = DoubleChars.topLeft,
        endCh = DoubleChars.topRight
      ))(),
      
      props.title.toOption.map { title =>
        <(textLineComp())(^.plain := TextLineProps(
          align = TextAlign.center,
          left = left,
          top = top,
          width = props.width,
          text = title,
          style = props.style
        ))()
      },

      <(verticalLineComp())(^.plain := VerticalLineProps(
        left = left,
        top = top + 1,
        length = props.height - 2,
        lineCh = DoubleChars.vertical,
        style = props.style
      ))(),
      
      <(verticalLineComp())(^.plain := VerticalLineProps(
        left = left + props.width - 1,
        top = top + 1,
        length = props.height - 2,
        lineCh = DoubleChars.vertical,
        style = props.style
      ))(),

      <(horizontalLineComp())(^.plain := HorizontalLineProps(
        left = left,
        top = top + props.height - 1,
        length = props.width,
        lineCh = DoubleChars.horizontal,
        style = props.style,
        startCh = DoubleChars.bottomLeft,
        endCh = DoubleChars.bottomRight
      ))()
    )
  }
}
