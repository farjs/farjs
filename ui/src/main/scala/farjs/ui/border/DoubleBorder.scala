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
      <(horizontalLineComp())(^.wrapped := HorizontalLineProps(
        pos = (left, top),
        length = props.width,
        lineCh = DoubleChars.horizontal,
        style = props.style,
        startCh = Some(DoubleChars.topLeft),
        endCh = Some(DoubleChars.topRight)
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

      <(verticalLineComp())(^.wrapped := VerticalLineProps(
        pos = (left, top + 1),
        length = props.height - 2,
        lineCh = DoubleChars.vertical,
        style = props.style
      ))(),
      
      <(verticalLineComp())(^.wrapped := VerticalLineProps(
        pos = (left + props.width - 1, top + 1),
        length = props.height - 2,
        lineCh = DoubleChars.vertical,
        style = props.style
      ))(),

      <(horizontalLineComp())(^.wrapped := HorizontalLineProps(
        pos = (left, top + props.height - 1),
        length = props.width,
        lineCh = DoubleChars.horizontal,
        style = props.style,
        startCh = Some(DoubleChars.bottomLeft),
        endCh = Some(DoubleChars.bottomRight)
      ))()
    )
  }
}
