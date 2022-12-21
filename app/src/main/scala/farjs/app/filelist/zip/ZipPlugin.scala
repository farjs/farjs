package farjs.app.filelist.zip

import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack.{PanelStackItem, WithPanelStacksProps}
import scommons.nodejs.path
import scommons.react.ReactClass

import scala.concurrent.Future
import scala.scalajs.js.typedarray.Uint8Array

object ZipPlugin extends FileListPlugin {

  private[zip] var readZip: String => Future[Map[String, List[ZipEntry]]] = ZipApi.readZip
  private[zip] var createApi: (String, String, Future[Map[String, List[ZipEntry]]]) => ZipApi = ZipApi.apply

  override val triggerKey: Option[String] = Some("S-f7")

  override def onKeyTrigger(stacks: WithPanelStacksProps): Option[ReactClass] = {
    val stackItem = stacks.activeStack.peek[FileListState]
    stackItem.getActions.zip(stackItem.state).flatMap { case ((dispatch, actions), state) =>
      val items =
        if (state.selectedNames.nonEmpty) state.selectedItems
        else state.currentItem.filter(_ != FileListItem.up).toList
      
      if (actions.isLocalFS && items.nonEmpty) {
        val zipName = s"${items.head.name}.zip"
        val ui = new ZipPluginUi(FileListData(dispatch, actions, state), zipName, items)
        Some(ui.apply())
      }
      else None
    }
  }
  
  override def onFileTrigger(filePath: String,
                             fileHeader: Uint8Array,
                             onClose: () => Unit): Option[PanelStackItem[FileListState]] = {
    val pathLower = filePath.toLowerCase
    if (pathLower.endsWith(".zip") || pathLower.endsWith(".jar") || checkFileHeader(fileHeader)) {
      val fileName = path.parse(filePath).base
      val rootPath = s"zip://$fileName"
      val entriesByParentF = readZip(filePath)
      
      Some(PanelStackItem(
        component = new FileListPanelController(new ZipPanel(filePath, rootPath, entriesByParentF, onClose)).apply(),
        dispatch = None,
        actions = Some(new ZipActions(createApi(filePath, rootPath, entriesByParentF))),
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
