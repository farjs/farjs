package farjs.ui

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object app {

  @js.native
  @JSImport("@farjs/ui/app/AppRoot.mjs", JSImport.Default)
  object AppRoot extends ReactClass

}
