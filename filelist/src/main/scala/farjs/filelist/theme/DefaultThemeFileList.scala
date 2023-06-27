package farjs.filelist.theme

import farjs.ui.theme.Color
import scommons.react.blessed.BlessedStyle

object DefaultThemeFileList extends ThemeFileList {

  val archiveItem: BlessedStyle = new BlessedStyle {
    override val bold = false
    override val bg = Color.blue
    override val fg = Color.magenta
    override val focus = new BlessedStyle {
      override val bold = false
      override val bg = Color.cyan
      override val fg = Color.magenta
    }
  }
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
