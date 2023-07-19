package farjs.ui.popup

import scommons.react._

object Modal extends FunctionComponent[ModalProps] {

  private[popup] var popupComp: ReactClass = Popup
  private[popup] var modalContentComp: UiComponent[ModalContentProps] = ModalContent
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain

    <(popupComp)(^.plain := PopupProps(onClose = props.onCancel))(
      <(modalContentComp())(^.plain := ModalContentProps(
        title = props.title,
        width = props.width,
        height = props.height,
        style = props.style
      ))(
        compProps.children
      )
    )
  }
}
