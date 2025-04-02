package farjs

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object viewer {

  @js.native
  @JSImport("../viewer/ViewerHeader.mjs", JSImport.Default)
  object ViewerHeader extends ReactClass

  @js.native
  @JSImport("../viewer/ViewerInput.mjs", JSImport.Default)
  object ViewerInput extends ReactClass

  @js.native
  @JSImport("../viewer/ViewerSearch.mjs", JSImport.Default)
  object ViewerSearch extends ReactClass
}
