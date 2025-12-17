package farjs

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object archiver {

  @js.native
  @JSImport("../archiver/AddToArchController.mjs", JSImport.Default)
  object AddToArchController extends ReactClass

  @js.native
  @JSImport("../archiver/ArchiverPluginUi.mjs", JSImport.Default)
  object ArchiverPluginUi extends js.Function1[ArchiverPluginUiParams, ReactClass] {
    
    def apply(params: ArchiverPluginUiParams): ReactClass = js.native
  }
}
