package farjs.ui.popup

import scommons.react._
import scommons.react.blessed._

import scala.scalajs.js

case class ModalProps(title: String,
                      size: (Int, Int),
                      style: BlessedStyle,
                      onCancel: () => Unit)

object Modal extends FunctionComponent[ModalProps] {

  private[popup] var popupComp: ReactClass = Popup
  private[popup] var modalContentComp: UiComponent[ModalContentProps] = ModalContent
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (width, height) = props.size

    <(popupComp)(^.plain := PopupProps(onClose = props.onCancel: js.Function0[Unit]))(
      <(modalContentComp())(^.plain := ModalContentProps(
        title = props.title,
        width = width,
        height = height,
        style = props.style
      ))(
        compProps.children
      )
    )
  }
}
