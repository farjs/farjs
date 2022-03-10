package farjs.app.filelist.zip

import farjs.filelist.stack.PanelStackItem
import farjs.filelist.{FileListPanelController, FileListPlugin, FileListState}

object ZipPlugin extends FileListPlugin {

  override def onFileTrigger(filePath: String, onClose: () => Unit): Option[PanelStackItem[FileListState]] = {
    val pathLower = filePath.toLowerCase
    if (pathLower.endsWith(".zip") || pathLower.endsWith(".jar")) {
      Some(PanelStackItem(
        component = new FileListPanelController(new ZipPanel(filePath, onClose)).apply(),
        dispatch = None,
        actions = Some(new ZipActions(filePath)),
        state = Some(FileListState())
      ))
    }
    else None
  }
}
