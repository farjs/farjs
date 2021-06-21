package farjs.ui.theme

import scommons.react.blessed.{BlessedStyle, Color}

object XTerm256Theme extends Theme {

  val fileList: ThemeFileList = new ThemeFileList {

    val regularItem: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "#008"
      override val fg = "#5ce"
      override val focus = new BlessedStyle {
        override val bold = true
        override val bg = "#088"
        override val fg = "#111"
      }
    }
    val dirItem: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "#008"
      override val fg = "#fff"
      override val focus = new BlessedStyle {
        override val bold = true
        override val bg = "#088"
        override val fg = "#fff"
      }
    }
    val hiddenItem: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "#008"
      override val fg = "#055"
      override val focus = new BlessedStyle {
        override val bold = true
        override val bg = "#088"
        override val fg = "#055"
      }
    }
    val selectedItem: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "#008"
      override val fg = Color.yellow
      override val focus = new BlessedStyle {
        override val bold = true
        override val bg = "#088"
        override val fg = Color.yellow
      }
    }
    val header: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "#008"
      override val fg = Color.yellow
    }
  }
  
  val popup: ThemePopup = new ThemePopup {

    val regular: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = Color.white
      override val fg = "#111"
      override val focus = new BlessedStyle {
        override val bold = true
        override val bg = "#088"
        override val fg = "#111"
      }
    }
    val error: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = Color.red
      override val fg = Color.white
      override val focus = new BlessedStyle {
        override val bold = true
        override val bg = Color.white
        override val fg = "#111"
      }
    }
    val menu: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "#088"
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
      override val bold = true
      override val bg = Color.black
      override val fg = "#aaa"
    }
    val item: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "#088"
      override val fg = "#111"
    }
  }
  
  val textBox: ThemeTextBox = new ThemeTextBox {

    val regular: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "#088"
      override val fg = "#111"
    }
    val selected: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = Color.blue
      override val fg = Color.white
    }
  }
}
