package farjs.ui.theme

import scommons.react.blessed.{BlessedStyle, Color}

object DefaultTheme extends Theme {

  val fileList: ThemeFileList = new ThemeFileList {

    val regularItem: BlessedStyle = new BlessedStyle {
      override val bold = false
      override val bg = Color.blue
      override val fg = Color.white
      override val focus = new BlessedStyle {
        override val bold = false
        override val bg = Color.cyan
        override val fg = Color.black
      }
    }
    val dirItem: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = Color.blue
      override val fg = Color.white
      override val focus = new BlessedStyle {
        override val bold = true
        override val bg = Color.cyan
        override val fg = Color.white
      }
    }
    val hiddenItem: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = Color.blue
      override val fg = Color.black
      override val focus = new BlessedStyle {
        override val bold = true
        override val bg = Color.cyan
        override val fg = Color.black
      }
    }
    val selectedItem: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = Color.blue
      override val fg = Color.yellow
      override val focus = new BlessedStyle {
        override val bold = true
        override val bg = Color.cyan
        override val fg = Color.yellow
      }
    }
    val header: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = Color.blue
      override val fg = Color.yellow
    }
  }
  
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
