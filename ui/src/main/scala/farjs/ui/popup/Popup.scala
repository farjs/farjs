package farjs.ui.popup

import farjs.ui.portal.Portal
import scommons.react._

case class PopupProps(onClose: () => Unit,
                      closable: Boolean = true,
                      focusable: Boolean = true,
                      onOpen: () => Unit = () => (),
                      onKeypress: String => Boolean = _ => false)

object Popup extends FunctionComponent[PopupProps] {
  
  private[popup] var portalComp: UiComponent[Unit] = Portal
  private[popup] var popupOverlayComp: UiComponent[PopupProps] = PopupOverlay
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    <(portalComp())()(
      <(popupOverlayComp())(^.wrapped := props)(
        compProps.children
      )
    )
  }
}
