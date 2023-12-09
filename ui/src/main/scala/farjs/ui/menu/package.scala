package farjs.ui

import scommons.react.ReactClass
import scommons.react.blessed.BlessedPadding

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object menu {

  @js.native
  @JSImport("@farjs/ui/menu/BottomMenu.mjs", JSImport.Default)
  object BottomMenu extends ReactClass

  @js.native
  @JSImport("@farjs/ui/menu/BottomMenuView.mjs", JSImport.Default)
  object BottomMenuView extends ReactClass

  @js.native
  @JSImport("@farjs/ui/menu/SubMenu.mjs", JSImport.Default)
  object SubMenu extends ReactClass {
    
    val separator: String = js.native
  }

  @js.native
  @JSImport("@farjs/ui/menu/MenuBar.mjs", JSImport.Default)
  object MenuBar extends ReactClass

  @js.native
  @JSImport("@farjs/ui/menu/MenuBarTrigger.mjs", JSImport.Default)
  object MenuBarTrigger extends ReactClass

  @js.native
  @JSImport("@farjs/ui/menu/MenuPopup.mjs", JSImport.Default)
  object MenuPopup extends ReactClass {
    
    def getLeftPos(stackWidth: Int, showOnLeft: Boolean, width: Int): String = js.native
    
    val padding: BlessedPadding = js.native
  }

}
