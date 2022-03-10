package farjs.app.filelist.zip

import farjs.app.filelist.zip.ZipPanel._
import farjs.filelist.api.FileListItem
import farjs.filelist.{FileListPanel, FileListPanelProps}
import scommons.react._
import scommons.react.blessed.BlessedScreen

class ZipPanel(filePath: String, onClose: () => Unit)
  extends FunctionComponent[FileListPanelProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    def onKeypress(screen: BlessedScreen, key: String): Boolean = {
      var processed = true
      key match {
        case "enter"
          if props.state.currentItem.exists(i => i.isDir && i.name == FileListItem.up.name)
            && props.state.currDir.path == s"ZIP://$filePath" =>
          onClose()
        case _ =>
          processed = false
      }

      processed
    }

    <(fileListPanelComp())(^.wrapped := props.copy(onKeypress = onKeypress))()
  }
}

object ZipPanel {

  private[zip] var fileListPanelComp: UiComponent[FileListPanelProps] = FileListPanel
}
