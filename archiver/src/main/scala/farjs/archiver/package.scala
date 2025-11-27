package farjs

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object archiver {

  @js.native
  @JSImport("../archiver/AddToArchPopup.mjs", JSImport.Default)
  object AddToArchPopup extends ReactClass
}
