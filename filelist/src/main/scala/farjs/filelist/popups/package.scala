package farjs.filelist

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object popups {

  @js.native
  @JSImport("../filelist/popups/HelpController.mjs", JSImport.Default)
  object HelpController extends ReactClass
}