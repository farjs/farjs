package farjs.filelist.theme

import farjs.ui.theme.Theme
import scommons.react.blessed.BlessedStyle

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("@farjs/filelist/theme/FileListTheme.mjs", JSImport.Default)
object FileListTheme extends js.Object {

  def useTheme(): FileListTheme = js.native
  
  val defaultTheme: FileListTheme = js.native

  val xterm256Theme: FileListTheme = js.native
}

trait FileListTheme extends Theme {

  val fileList: ThemeFileList
}

trait ThemeFileList extends js.Object {

  val archiveItem: BlessedStyle
  val regularItem: BlessedStyle
  val dirItem: BlessedStyle
  val hiddenItem: BlessedStyle
  val selectedItem: BlessedStyle
  val header: BlessedStyle
}
