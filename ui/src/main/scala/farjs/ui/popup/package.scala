package farjs.ui

import scommons.react.ReactClass
import scommons.react.blessed.{BlessedPadding, BlessedStyle}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object popup {

  @js.native
  @JSImport("@farjs/ui/popup/ListPopup.mjs", JSImport.Default)
  object ListPopup extends ReactClass

  @js.native
  @JSImport("@farjs/ui/popup/MessageBox.mjs", JSImport.Default)
  object MessageBox extends ReactClass

  @js.native
  @JSImport("@farjs/ui/popup/MessageBoxAction.mjs", JSImport.Default)
  object MessageBoxActionNative extends js.Object {
    
    def OK(onAction: js.Function0[Unit]): MessageBoxAction = js.native
    def YES(onAction: js.Function0[Unit]): MessageBoxAction = js.native
    def NO(onAction: js.Function0[Unit]): MessageBoxAction = js.native
  }

  @js.native
  @JSImport("@farjs/ui/popup/Modal.mjs", JSImport.Default)
  object Modal extends ReactClass

  @js.native
  @JSImport("@farjs/ui/popup/ModalContent.mjs", JSImport.Default)
  object ModalContent extends ReactClass {

    val paddingHorizontal: Int = js.native
    val paddingVertical: Int = js.native
    val padding: BlessedPadding = js.native
  }

  @js.native
  @JSImport("@farjs/ui/popup/Popup.mjs", JSImport.Default)
  object Popup extends ReactClass

  @js.native
  @JSImport("@farjs/ui/popup/PopupOverlay.mjs", JSImport.Default)
  object PopupOverlay extends ReactClass {

    val style: BlessedStyle = js.native
  }

  @js.native
  @JSImport("@farjs/ui/popup/StatusPopup.mjs", JSImport.Default)
  object StatusPopup extends ReactClass
}
