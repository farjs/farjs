package farjs.viewer

import farjs.ui._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._

case class ViewerHeaderProps(filePath: String,
                             encoding: String = "",
                             size: Double = 0,
                             column: Int = 0,
                             percent: Int = 0)

object ViewerHeader extends FunctionComponent[ViewerHeaderProps] {

  private[viewer] var withSizeComp: ReactClass = WithSize
  private[viewer] var textLineComp: ReactClass = TextLine

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val style = Theme.useTheme().menu.item
    val encodingWidth = math.max(props.encoding.length, 10)
    val sizeText = f"${props.size}%,.0f"
    val sizeWidth = math.max(sizeText.length, 12)
    val columnWidth = 8
    val percentWidth = 4
    val gapWidth = 2

    <(withSizeComp)(^.plain := WithSizeProps({ (width, _) =>
      val dynamicWidth =
        width - encodingWidth - sizeWidth - columnWidth - percentWidth - gapWidth * 3

      <.box(^.rbStyle := style)(
        <(textLineComp)(^.plain := TextLineProps(
          align = TextAlign.left,
          left = 0,
          top = 0,
          width = dynamicWidth,
          text = props.filePath,
          style = style,
          padding = 0
        ))(),
        <(textLineComp)(^.plain := TextLineProps(
          align = TextAlign.center,
          left = dynamicWidth + gapWidth,
          top = 0,
          width = encodingWidth,
          text = props.encoding,
          style = style,
          padding = 0
        ))(),
        <(textLineComp)(^.plain := TextLineProps(
          align = TextAlign.right,
          left = dynamicWidth + encodingWidth + gapWidth * 2,
          top = 0,
          width = sizeWidth,
          text = sizeText,
          style = style,
          padding = 0
        ))(),
        <(textLineComp)(^.plain := TextLineProps(
          align = TextAlign.left,
          left = width - columnWidth - percentWidth,
          top = 0,
          width = columnWidth,
          text = s"Col ${props.column}",
          style = style,
          padding = 0
        ))(),
        <(textLineComp)(^.plain := TextLineProps(
          align = TextAlign.right,
          left = width - percentWidth,
          top = 0,
          width = percentWidth,
          text = s"${props.percent}%",
          style = style,
          padding = 0
        ))()
      )
    }))()
  }
}
