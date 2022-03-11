package farjs.app.filelist.zip

import farjs.filelist.stack.PanelStackItem
import farjs.filelist.{FileListPanelController, FileListPlugin, FileListState}
import scommons.nodejs.path

object ZipPlugin extends FileListPlugin {

  override def onFileTrigger(filePath: String, onClose: () => Unit): Option[PanelStackItem[FileListState]] = {
    val pathLower = filePath.toLowerCase
    if (pathLower.endsWith(".zip") || pathLower.endsWith(".jar")) {
      val fileName = path.parse(filePath).base
      val rootPath = s"ZIP://$fileName"
      
      Some(PanelStackItem(
        component = new FileListPanelController(new ZipPanel(rootPath, onClose)).apply(),
        dispatch = None,
        actions = Some(new ZipActions(new ZipApi(filePath, rootPath))),
        state = Some(FileListState())
      ))
    }
    else None
  }
}
