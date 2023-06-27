package farjs.filelist.theme

import farjs.ui.theme.{DefaultTheme, Theme, XTerm256Theme}
import scommons.react.blessed.BlessedStyle

import scala.scalajs.js
import scala.scalajs.js.|

object FileListTheme {

  def useTheme: FileListTheme = {
    Theme.useTheme().asInstanceOf[FileListTheme]
  }
  
  lazy val defaultTheme: FileListTheme = {
    mergeJSObjects[FileListTheme](js.Dynamic.literal(
      fileList = DefaultThemeFileList
    ), DefaultTheme)
  }

  lazy val xterm256Theme: FileListTheme = {
    mergeJSObjects[FileListTheme](js.Dynamic.literal(
      fileList = XTerm256ThemeFileList
    ), XTerm256Theme)
  }

  private def mergeJSObjects[T <: js.Object](objs: (js.Object | js.Dynamic)*): T = {
    val result = js.Dictionary.empty[js.Any]
    for (source <- objs) {
      for ((key, value) <- source.asInstanceOf[js.Dictionary[js.Any]]) {
        result(key) = value
      }
    }
    result.asInstanceOf[T]
  }
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
