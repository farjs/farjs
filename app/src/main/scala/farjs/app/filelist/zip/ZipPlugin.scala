package farjs.app.filelist.zip

import farjs.filelist.api.FileListDir
import farjs.filelist.stack.PanelStackItem
import farjs.filelist.{FileListPanelController, FileListPlugin, FileListState}
import scommons.nodejs.{ChildProcess, child_process, path}

import scala.concurrent.Future

class ZipPlugin(childProcess: ChildProcess,
                readZip: (ChildProcess, String) => Future[List[ZipEntry]]
               ) extends FileListPlugin {

  override def onFileTrigger(filePath: String, onClose: () => Unit): Option[PanelStackItem[FileListState]] = {
    val pathLower = filePath.toLowerCase
    if (pathLower.endsWith(".zip") || pathLower.endsWith(".jar")) {
      val fileName = path.parse(filePath).base
      val rootPath = s"zip://$fileName"
      val entriesF = readZip(childProcess, filePath)
      
      Some(PanelStackItem(
        component = new FileListPanelController(new ZipPanel(rootPath, entriesF, onClose)).apply(),
        dispatch = None,
        actions = Some(new ZipActions(new ZipApi(filePath, rootPath, entriesF))),
        state = Some(FileListState(
          currDir = FileListDir(rootPath, isRoot = false, items = Nil)
        ))
      ))
    }
    else None
  }
}

object ZipPlugin extends ZipPlugin(child_process, ZipApi.readZip)
