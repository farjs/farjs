package farjs.ui.popup

import farjs.ui.border._
import scommons.react._
import scommons.react.blessed._

case class ModalContentProps(title: String,
                             size: (Int, Int),
                             style: BlessedStyle,
                             padding: BlessedPadding = ModalContent.padding)

object ModalContent extends FunctionComponent[ModalContentProps] {

  private[popup] var doubleBorderComp: UiComponent[DoubleBorderProps] = DoubleBorder
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (width, height) = props.size
    val padding = props.padding

    <.box(
      ^.rbClickable := true,
      ^.rbAutoFocus := false,
      ^.rbWidth := width,
      ^.rbHeight := height,
      ^.rbTop := "center",
      ^.rbLeft := "center",
      ^.rbShadow := true,
      ^.rbPadding := padding,
      ^.rbStyle := props.style
    )(
      <(doubleBorderComp())(^.wrapped := DoubleBorderProps(
        size = (width - padding.left - padding.right, height - padding.top - padding.bottom),
        style = props.style,
        pos = (0, 0),
        title = Some(props.title)
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
