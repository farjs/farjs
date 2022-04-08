package farjs.app.filelist.zip

import farjs.filelist.api.FileListDir
import farjs.filelist.stack.PanelStackItem
import farjs.filelist.{FileListPanelController, FileListPlugin, FileListState}
import scommons.nodejs.{ChildProcess, child_process, path}

import scala.concurrent.Future
import scala.scalajs.js.typedarray.Uint8Array

class ZipPlugin(childProcess: ChildProcess,
                readZip: (ChildProcess, String) => Future[Map[String, List[ZipEntry]]]
               ) extends FileListPlugin {

  override def onFileTrigger(filePath: String,
                             fileHeader: Uint8Array,
                             onClose: () => Unit): Option[PanelStackItem[FileListState]] = {
    val pathLower = filePath.toLowerCase
    if (pathLower.endsWith(".zip") || pathLower.endsWith(".jar") || checkFileHeader(fileHeader)) {
      val fileName = path.parse(filePath).base
      val rootPath = s"zip://$fileName"
      val entriesByParentF = readZip(childProcess, filePath)
      
      Some(PanelStackItem(
        component = new FileListPanelController(new ZipPanel(rootPath, entriesByParentF, onClose)).apply(),
        dispatch = None,
        actions = Some(new ZipActions(new ZipApi(childProcess, filePath, rootPath, entriesByParentF))),
        state = Some(FileListState(
          currDir = FileListDir(rootPath, isRoot = false, items = Nil)
        ))
      ))
    }
    else None
  }
  
  private def checkFileHeader(header: Uint8Array): Boolean = {
    if (header.length < 4) false
    else {
      header(0) == 'P' &&
        header(1) == 'K' &&
        header(2) == 0x03 &&
        header(3) == 0x04
    }
  }
}

object ZipPlugin extends ZipPlugin(child_process, ZipApi.readZip)
