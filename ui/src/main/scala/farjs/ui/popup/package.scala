package farjs.ui

import scommons.react.ReactClass
import scommons.react.blessed.BlessedStyle

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object popup {

  @js.native
  @JSImport("@farjs/ui/popup/Popup.mjs", JSImport.Default)
  object Popup extends ReactClass

  @js.native
  @JSImport("@farjs/ui/popup/PopupOverlay.mjs", JSImport.Default)
  object PopupOverlay extends ReactClass {

    val style: BlessedStyle = js.native
  }
}
