package farjs.filelist.theme

import scommons.react.blessed.{BlessedStyle, Color}

object XTerm256ThemeFileList extends ThemeFileList {

  val archiveItem: BlessedStyle = new BlessedStyle {
    override val bold = true
    override val bg = "#008"
    override val fg = "#a05"
    override val focus = new BlessedStyle {
      override val bold = true
      override val bg = "#088"
      override val fg = "#a05"
    }
  }
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
