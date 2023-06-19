package farjs.ui.tool

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
sealed trait DevTool extends js.Object

@js.native
@JSImport("@farjs/ui/tool/DevTool.mjs", JSImport.Default)
object DevTool extends js.Object {

  def shouldResize(from: DevTool, to: DevTool): Boolean = js.native

  def getNext(from: DevTool): DevTool = js.native

  val Hidden: DevTool = js.native
  val Logs: DevTool = js.native
  val Inputs: DevTool = js.native
  val Colors: DevTool = js.native
}
