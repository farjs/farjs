package farjs.app.raw

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobalScope

@js.native
@JSGlobalScope
object NodeGlobal extends js.Object {

  def require[T <: js.Object](module: String): T = js.native
}
