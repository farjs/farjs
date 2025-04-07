package farjs.viewer

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object quickview {

  @js.native
  @JSImport("../viewer/quickview/QuickViewDir.mjs", JSImport.Default)
  object QuickViewDir extends ReactClass
}
