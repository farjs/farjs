package farjs.ui.theme

import scommons.react.blessed.BlessedStyle
import scommons.react.raw.NativeContext

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@farjs/ui/theme/XTerm256Theme.mjs", JSImport.Default)
object XTerm256Theme extends Theme {

  val popup: ThemePopup = js.native
  val menu: ThemeMenu = js.native
  val textBox: ThemeTextBox = js.native
}

@js.native
@JSImport("@farjs/ui/theme/DefaultTheme.mjs", JSImport.Default)
object DefaultTheme extends Theme {

  val popup: ThemePopup = js.native
  val menu: ThemeMenu = js.native
  val textBox: ThemeTextBox = js.native
}

@js.native
@JSImport("@farjs/ui/theme/Theme.mjs", JSImport.Default)
object Theme extends js.Object {

  val Context: NativeContext = js.native

  def useTheme(): Theme = js.native
}

trait Theme extends js.Object {

  val popup: ThemePopup
  val menu: ThemeMenu
  val textBox: ThemeTextBox
}

trait ThemePopup extends js.Object {

  val regular: BlessedStyle
  val error: BlessedStyle
  val menu: BlessedStyle
}

trait ThemeMenu extends js.Object {

  val key: BlessedStyle
  val item: BlessedStyle
}

trait ThemeTextBox extends js.Object {

  val regular: BlessedStyle
  val selected: BlessedStyle
}
