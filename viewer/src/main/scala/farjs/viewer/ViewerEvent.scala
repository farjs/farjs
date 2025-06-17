package farjs.viewer

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("../viewer/ViewerEvent.mjs", JSImport.Default)
object ViewerEvent extends js.Object {

  val onViewerOpenLeft: String = js.native
  val onViewerOpenRight: String = js.native
}
