package farjs.ui.popup

import farjs.ui.border._
import scommons.react._
import scommons.react.blessed._

case class ModalContentProps(title: String,
                             size: (Int, Int),
                             style: BlessedStyle,
                             padding: BlessedPadding = ModalContent.padding,
                             left: String = "center",
                             footer: Option[String] = None)

object ModalContent extends FunctionComponent[ModalContentProps] {

  private[popup] var doubleBorderComp: ReactClass = DoubleBorder
  
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
      ^.rbLeft := props.left,
      ^.rbShadow := true,
      ^.rbPadding := padding,
      ^.rbStyle := props.style
    )(
      <(doubleBorderComp)(^.plain := DoubleBorderProps(
        width = width - padding.left - padding.right,
        height = height - padding.top - padding.bottom,
        style = props.style,
        title = props.title,
        footer = props.footer match {
          case None => ()
          case Some(footer) => footer
        }
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
