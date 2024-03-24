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
  @JSImport("@farjs/ui/CheckBox.mjs", JSImport.Default)
  object CheckBox extends ReactClass

  @js.native
  @JSImport("@farjs/ui/ComboBoxPopup.mjs", JSImport.Default)
  object ComboBoxPopup extends ReactClass {
    
    val maxItems: Int = js.native
  }

  @js.native
  @JSImport("@farjs/ui/ListBox.mjs", JSImport.Default)
  object ListBox extends ReactClass

  @js.native
  @JSImport("@farjs/ui/ListView.mjs", JSImport.Default)
  object ListView extends ReactClass

  @js.native
  @JSImport("@farjs/ui/ProgressBar.mjs", JSImport.Default)
  object ProgressBar extends ReactClass

  @js.native
  @JSImport("@farjs/ui/ScrollBar.mjs", JSImport.Default)
  object ScrollBar extends ReactClass

  @js.native
  @JSImport("@farjs/ui/TextInput.mjs", JSImport.Default)
  object TextInput extends ReactClass

  @js.native
  @JSImport("@farjs/ui/TextLine.mjs", JSImport.Default)
  object TextLine extends ReactClass {

    def wrapText(text: String, width: Int, prefixLen: js.UndefOr[Int] = js.undefined): String = js.native
  }

  @js.native
  @JSImport("@farjs/ui/WithSize.mjs", JSImport.Default)
  object WithSize extends ReactClass

}
