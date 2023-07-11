package farjs.ui

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
sealed trait TextAlign extends js.Object

@js.native
@JSImport("@farjs/ui/TextAlign.mjs", JSImport.Default)
object TextAlign extends js.Object {
  
  val left: TextAlign = js.native
  val right: TextAlign = js.native
  val center: TextAlign = js.native
}
