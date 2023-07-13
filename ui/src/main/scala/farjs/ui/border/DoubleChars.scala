package farjs.ui.border

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@farjs/ui/border/DoubleChars.mjs", JSImport.Default)
object DoubleChars extends js.Object {

  // lines
  val horizontal: String = js.native
  val vertical: String = js.native

  // corners
  val topLeft: String = js.native
  val topRight: String = js.native
  val bottomLeft: String = js.native
  val bottomRight: String = js.native

  // connectors
  val top: String = js.native
  val bottom: String = js.native
  val left: String = js.native
  val right: String = js.native

  // single connectors
  val topSingle: String = js.native
  val bottomSingle: String = js.native
  val leftSingle: String = js.native
  val rightSingle: String = js.native

  // crosses
  val cross: String = js.native
  val crossSingleVert: String = js.native
  val crossSingleHoriz: String = js.native
}
