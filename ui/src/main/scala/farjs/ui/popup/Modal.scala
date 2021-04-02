package farjs.ui.popup

import scommons.react._
import scommons.react.blessed._

case class ModalProps(title: String,
                      size: (Int, Int),
                      style: BlessedStyle,
                      onCancel: () => Unit)

object Modal extends FunctionComponent[ModalProps] {

  private[popup] var popupComp: UiComponent[PopupProps] = Popup
  private[popup] var modalContentComp: UiComponent[ModalContentProps] = ModalContent
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    <(popupComp())(^.wrapped := PopupProps(onClose = props.onCancel))(
      <(modalContentComp())(^.wrapped := ModalContentProps(
        title = props.title,
        size = props.size,
        style = props.style
      ))(
        compProps.children
      )
    )
  }
}
