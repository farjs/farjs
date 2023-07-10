package farjs.ui.menu

import farjs.ui._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._

import scala.scalajs.js

case class MenuPopupProps(title: String,
                          items: List[String],
                          getLeft: Int => String,
                          onSelect: Int => Unit,
                          onClose: () => Unit)

object MenuPopup extends FunctionComponent[MenuPopupProps] {

  private[menu] var popupComp: ReactClass = Popup
  private[menu] var modalContentComp: UiComponent[ModalContentProps] = ModalContent
  private[menu] var buttonComp: ReactClass = Button

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    val textWidth = props.items.maxBy(_.length).length
    val width = textWidth + (paddingHorizontal + 1) * 2
    val height = (paddingVertical + 1) * 2 + props.items.size
    val theme = Theme.useTheme().popup.menu

    <(popupComp)(^.plain := PopupProps(onClose = props.onClose: js.Function0[Unit]))(
      <(modalContentComp())(^.wrapped := ModalContentProps(
        title = props.title,
        size = (width, height),
        style = theme,
        padding = padding,
        left = props.getLeft(width)
      ))(
        props.items.zipWithIndex.map { case (text, index) =>
          <(buttonComp)(^.key := s"$index", ^.plain := ButtonProps(
            left = 1,
            top = 1 + index,
            label = text,
            style = theme,
            onPress = { () =>
              props.onSelect(index)
            }
          ))()
        }
      )
    )
  }

  private val paddingHorizontal = 2
  private val paddingVertical = 1
  private[menu] val padding = new BlessedPadding {
    val left: Int = paddingHorizontal
    val right: Int = paddingHorizontal
    val top: Int = paddingVertical
    val bottom: Int = paddingVertical
  }
  
  def getLeftPos(stackWidth: Int, showOnLeft: Boolean, width: Int): String = {
    val pos =
      if (width <= stackWidth) (stackWidth - width) / 2
      else stackWidth - width
    val (left, normalizedPos) =
      if (showOnLeft || width > stackWidth * 2) ("0%", math.max(pos, 0))
      else ("50%", pos)

    if (normalizedPos >= 0) s"$left+$normalizedPos"
    else s"$left$normalizedPos"
  }
}
