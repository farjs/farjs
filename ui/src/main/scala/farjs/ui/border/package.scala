package farjs.ui

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object border {

  @js.native
  @JSImport("@farjs/ui/border/DoubleBorder.mjs", JSImport.Default)
  object DoubleBorder extends ReactClass

  @js.native
  @JSImport("@farjs/ui/border/HorizontalLine.mjs", JSImport.Default)
  object HorizontalLine extends ReactClass

  @js.native
  @JSImport("@farjs/ui/border/VerticalLine.mjs", JSImport.Default)
  object VerticalLine extends ReactClass
}
