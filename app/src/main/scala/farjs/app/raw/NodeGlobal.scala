package farjs.app.raw

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSName}

@js.native
@JSGlobalScope
object NodeGlobal extends js.Object {

  @JSName("__non_webpack_require__")
  def require[T <: js.Object](module: String): T = js.native
}
