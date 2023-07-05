package farjs.ui

import scommons.react.ReactClass
import scommons.react.blessed.BlessedScreen

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object portal {

  @js.native
  @JSImport("@farjs/ui/portal/Portal.mjs", JSImport.Default)
  object Portal extends ReactClass

  @js.native
  @JSImport("@farjs/ui/portal/WithPortals.mjs", JSImport.Default)
  object WithPortals extends js.Object {

    def create(screen: BlessedScreen): ReactClass = js.native
  }
}
