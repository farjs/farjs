package farjs

import scommons.react.ReactClass

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

package object ui {

  type Dispatch = Any => Any

  @js.native
  @JSImport("@farjs/ui/Button.mjs", JSImport.Default)
  object Button extends ReactClass

  @js.native
  @JSImport("@farjs/ui/ButtonsPanel.mjs", JSImport.Default)
  object ButtonsPanel extends ReactClass

  @js.native
  @JSImport("@farjs/ui/TextLine.mjs", JSImport.Default)
  object TextLine extends ReactClass {

    def wrapText(text: String, width: Int, prefixLen: js.UndefOr[Int] = js.undefined): String = js.native
  }
}
