package farjs.filelist.fs

import farjs.filelist.stack.PanelStack
import farjs.ui._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._

case class FSDrivePopupProps(isRight: Boolean, onClose: () => Unit = () => ())

object FSDrivePopup extends FunctionComponent[FSDrivePopupProps] {

  private[fs] var popupComp: UiComponent[PopupProps] = Popup
  private[fs] var modalContentComp: UiComponent[ModalContentProps] = ModalContent
  private[fs] var buttonComp: UiComponent[ButtonProps] = Button

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val panelStack = PanelStack.usePanelStack
    val width = 50
    //val textWidth = width - 3 * 2
    val disks = List(
      FSDisk("C:", size = 156595318784.0, free = 81697124352.0, "SYSTEM"),
      FSDisk("D:", size = 842915639296.0, free = 352966430720.0, "DATA"),
      FSDisk("E:", size = 0.0, free = 0.0, "")
    )
    val height = 2 * 2 + disks.size
    val theme = Theme.current.popup.menu
    val leftPos = {
      val pos =
        if (width <= panelStack.width) (panelStack.width - width) / 2
        else panelStack.width - width
      val (left, normalizedPos) =
        if (!props.isRight || width > panelStack.width * 2) ("0%", math.max(pos, 0))
        else ("50%", pos)
      
      if (normalizedPos >= 0) s"$left+$normalizedPos"
      else s"$left$normalizedPos"
    }

    println(s"leftPos: $leftPos")
    <(popupComp())(^.wrapped := PopupProps(onClose = props.onClose))(
      <(modalContentComp())(^.wrapped := ModalContentProps(
        title = "Drive",
        size = (width, height),
        style = theme,
        padding = padding,
        left = leftPos
      ))(
        disks.zipWithIndex.map { case (disk, index) =>
          <(buttonComp())(^.key := s"$index", ^.wrapped := ButtonProps(
            pos = (1, 1 + index),
            label = disk.name,
            style = theme,
            onPress = { () =>
              //TODO: implement me
            }
          ))()
        }
      )
    )
  }

  private val padding = new BlessedPadding {
    val left: Int = 2
    val right: Int = 2
    val top: Int = 1
    val bottom: Int = 1
  }
}
