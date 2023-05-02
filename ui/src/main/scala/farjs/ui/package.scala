package farjs

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object ui {

  type Dispatch = Any => Any

  @js.native
  @JSImport("@farjs/ui/Button.mjs", JSImport.Default)
  object Button extends ReactClass
}
