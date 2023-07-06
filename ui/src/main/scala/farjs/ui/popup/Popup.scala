package farjs.ui.popup

import farjs.ui.portal.Portal
import scommons.react._

object Popup extends FunctionComponent[PopupProps] {
  
  private[popup] var portalComp: ReactClass = Portal
  private[popup] var popupOverlayComp: UiComponent[PopupProps] = PopupOverlay
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain

    <(portalComp)()(
      <(popupOverlayComp())(^.plain := props)(
        compProps.children
      )
    )
  }
}
