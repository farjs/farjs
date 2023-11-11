package farjs.ui

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object menu {

  @js.native
  @JSImport("@farjs/ui/menu/SubMenu.mjs", JSImport.Default)
  object SubMenu extends ReactClass {
    
    val separator: String = js.native
  }

}
