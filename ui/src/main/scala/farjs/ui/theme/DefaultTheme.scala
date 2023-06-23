package farjs.ui.theme

import scommons.react.blessed.{BlessedStyle, Color}

object DefaultTheme extends Theme {
  
  val popup: ThemePopup = new ThemePopup {

    val regular: BlessedStyle = new BlessedStyle {
      override val bold = false
      override val bg = Color.white
      override val fg = Color.black
      override val focus = new BlessedStyle {
        override val bold = false
        override val bg = Color.cyan
        override val fg = Color.black
      }
    }
    val error: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = Color.red
      override val fg = Color.white
      override val focus = new BlessedStyle {
        override val bold = false
        override val bg = Color.white
        override val fg = Color.black
      }
    }
    val menu: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = Color.cyan
      override val fg = Color.white
      override val focus = new BlessedStyle {
        override val bold = true
        override val bg = Color.black
        override val fg = Color.white
      }
    }
  }
  
  val menu: ThemeMenu = new ThemeMenu {

    val key: BlessedStyle = new BlessedStyle {
      override val bg = Color.black
      override val fg = Color.white
    }
    val item: BlessedStyle = new BlessedStyle {
      override val bg = Color.cyan
      override val fg = Color.black
    }
  }
  
  val textBox: ThemeTextBox = new ThemeTextBox {

    val regular: BlessedStyle = new BlessedStyle {
      override val bold = false
      override val bg = Color.cyan
      override val fg = Color.black
    }
    val selected: BlessedStyle = new BlessedStyle {
      override val bold = false
      override val bg = Color.blue
      override val fg = Color.white
    }
  }
}
