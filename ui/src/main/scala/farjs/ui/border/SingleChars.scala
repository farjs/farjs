package farjs.ui.border

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@farjs/ui/border/SingleChars.mjs", JSImport.Default)
object SingleChars extends js.Object {

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

  // double connectors
  val topDouble: String = js.native
  val bottomDouble: String = js.native
  val leftDouble: String = js.native
  val rightDouble: String = js.native

  // crosses
  val cross: String = js.native
  val crossDoubleVert: String = js.native
  val crossDoubleHoriz: String = js.native
}
