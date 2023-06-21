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

trait Theme {

  def fileList: ThemeFileList
  def popup: ThemePopup
  def menu: ThemeMenu
  def textBox: ThemeTextBox
}

trait ThemeFileList {

  def archiveItem: BlessedStyle
  def regularItem: BlessedStyle
  def dirItem: BlessedStyle
  def hiddenItem: BlessedStyle
  def selectedItem: BlessedStyle
  def header: BlessedStyle
}

trait ThemePopup {

  def regular: BlessedStyle
  def error: BlessedStyle
  def menu: BlessedStyle
}

trait ThemeMenu {

  def key: BlessedStyle
  def item: BlessedStyle
}

trait ThemeTextBox {

  def regular: BlessedStyle
  def selected: BlessedStyle
}
