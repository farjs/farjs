package farjs

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object copymove {

  @js.native
  @JSImport("../copymove/FileExistsPopup.mjs", JSImport.Default)
  object FileExistsPopup extends ReactClass
}
