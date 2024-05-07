package farjs.archiver

import farjs.archiver.zip._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack.{PanelStackItem, WithPanelStacksProps}
import scommons.nodejs.path
import scommons.react.ReactClass

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

object ArchiverPlugin extends FileListPlugin {

  private[archiver] var readZip: String => Future[Map[String, List[ZipEntry]]] = ZipApi.readZip
  private[archiver] var createApi: (String, String, Future[Map[String, List[ZipEntry]]]) => ZipApi = {
    (zipPath, rootPath, entriesByParentF) =>
      new ZipApi(zipPath, rootPath, entriesByParentF)
  }

  override val triggerKeys: js.Array[String] = js.Array("S-f7")

  override def onKeyTrigger(key: String,
                            stacks: WithPanelStacksProps,
                            data: js.UndefOr[js.Dynamic] = js.undefined): Future[Option[ReactClass]] = {

    val stackItem = stacks.activeStack.peek[FileListState]
    val res = stackItem.getData.flatMap { case FileListData(dispatch, actions, state) =>
      val items =
        if (state.selectedNames.nonEmpty) FileListState.selectedItems(state).toList
        else FileListState.currentItem(state).filter(_ != FileListItem.up).toList
      
      if (actions.api.isLocal && items.nonEmpty) {
        val zipName = s"${items.head.name}.zip"
        val ui = new ArchiverPluginUi(FileListData(dispatch, actions, state), zipName, items)
        Some(ui.apply())
      }
      else None
    }
    Future.successful(res)
  }
  
  override def onFileTrigger(filePath: String,
                             fileHeader: Uint8Array,
                             onClose: () => Unit): Future[Option[PanelStackItem[FileListState]]] = {
    val pathLower = filePath.toLowerCase
    val res =
      if (pathLower.endsWith(".zip") || pathLower.endsWith(".jar") || checkFileHeader(fileHeader)) {
        val fileName = path.parse(filePath).base
        val rootPath = s"zip://$fileName"
        val entriesByParentF = readZip(filePath)
        
        Some(PanelStackItem(
          component = new FileListPanelController(new ZipPanel(filePath, rootPath, entriesByParentF, onClose)).apply(),
          dispatch = None,
          actions = Some(new ZipActions(createApi(filePath, rootPath, entriesByParentF))),
          state = Some(FileListState(
            currDir = FileListDir(rootPath, isRoot = false, items = js.Array())
          ))
        ))
      }
      else None

    Future.successful(res)
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
