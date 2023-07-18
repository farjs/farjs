package farjs.ui.popup

import farjs.ui.border._
import scommons.react._
import scommons.react.blessed._

object ModalContent extends FunctionComponent[ModalContentProps] {

  private[popup] var doubleBorderComp: ReactClass = DoubleBorder
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val width = props.width
    val height = props.height
    val padding = props.padding.getOrElse(ModalContent.padding)
    val left = props.left.getOrElse("center")

    <.box(
      ^.rbClickable := true,
      ^.rbAutoFocus := false,
      ^.rbWidth := width,
      ^.rbHeight := height,
      ^.rbTop := "center",
      ^.rbLeft := left,
      ^.rbShadow := true,
      ^.rbPadding := padding,
      ^.rbStyle := props.style
    )(
      <(doubleBorderComp)(^.plain := DoubleBorderProps(
        width = width - padding.left - padding.right,
        height = height - padding.top - padding.bottom,
        style = props.style,
        title = props.title,
        footer = props.footer
      ))(),
      
      compProps.children
    )
  }

  val paddingHorizontal = 3
  val paddingVertical = 1

  val padding: BlessedPadding = new BlessedPadding {
    val left: Int = paddingHorizontal
    val right: Int = paddingHorizontal
    val top: Int = paddingVertical
    val bottom: Int = paddingVertical
  }
}
