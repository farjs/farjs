package scommons.nodejs.raw

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal("global")
object NodeJsNative extends js.Object {
  
  val process: NodeProcess = js.native
}

@js.native
trait NodeProcess extends js.Object {
  
  def exit(code: Int): Unit = js.native
}
