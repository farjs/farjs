package farjs.ui.theme

import scommons.react.blessed.BlessedStyle

object Theme {

  var current: Theme = DefaultTheme
}

trait Theme {

  def fileList: ThemeFileList
  def popup: ThemePopup
  def menu: ThemeMenu
  def textBox: ThemeTextBox
}

trait ThemeFileList {

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
