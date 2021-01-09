package farjs.ui.popup

import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.portal.Portal

case class PopupProps(onClose: () => Unit,
                      closable: Boolean = true,
                      focusable: Boolean = true,
                      onOpen: () => Unit = () => ())

object Popup extends FunctionComponent[PopupProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    Portal.create(
      <(PopupOverlay())(^.wrapped := props)(
        compProps.children
      )
    )
  }
  
  object Styles {

    val normal: BlessedStyle = new BlessedStyle {
      override val bold = false
      override val bg = "white"
      override val fg = "black"
      override val focus = new BlessedStyle {
        override val bold = false
        override val bg = "#088"
        override val fg = "black"
      }
    }
    
    val error: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "red"
      override val fg = "white"
      override val focus = new BlessedStyle {
        override val bold = false
        override val bg = "white"
        override val fg = "black"
      }
    }
  }
}
