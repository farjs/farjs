package farjs

import scommons.react.ReactClass

package object viewer {

  import scala.scalajs.js
  import scala.scalajs.js.annotation.JSImport

  @js.native
  @JSImport("../viewer/ViewerHeader.mjs", JSImport.Default)
  object ViewerHeader extends ReactClass
}
