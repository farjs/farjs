package farjs.file

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object popups {

  @js.native
  @JSImport("../file/popups/TextSearchPopup.mjs", JSImport.Default)
  object TextSearchPopup extends ReactClass
}
