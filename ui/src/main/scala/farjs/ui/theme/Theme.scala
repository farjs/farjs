package farjs.ui.theme

import scommons.react.ReactContext
import scommons.react.blessed.BlessedStyle
import scommons.react.hooks.useContext

import scala.scalajs.js

object Theme {

  val Context: ReactContext[Theme] = ReactContext[Theme](defaultValue = null)

  def useTheme: Theme = {
    val ctx = useContext(Context)
    if (ctx == null) {
      throw js.JavaScriptException(js.Error(
        "Theme.Context is not found." +
          "\nPlease, make sure you use Theme.Context.Provider in parent components"
      ))
    }
    ctx
  }
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
