package farjs.archiver

import farjs.archiver.zip._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack.{PanelStackItem, WithStacksProps}
import scommons.nodejs.path
import scommons.react.ReactClass

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Uint8Array

object ArchiverPlugin extends FileListPlugin(js.Array("S-f7")) {

  private[archiver] final var readZip: String => Future[Map[String, List[FileListItem]]] = ZipApi.readZip
  private[archiver] final var createApi: (String, String, Future[Map[String, List[FileListItem]]]) => ZipApi = {
    (zipPath, rootPath, entriesByParentF) =>
      new ZipApi(zipPath, rootPath, entriesByParentF)
  }

  override def onKeyTrigger(key: String,
                            stacks: WithStacksProps,
                            data: js.UndefOr[js.Dynamic] = js.undefined): js.Promise[js.UndefOr[ReactClass]] = {

    val res = createUiParams(stacks).map(ArchiverPluginUi)

    js.Promise.resolve[js.UndefOr[ReactClass]](res match {
      case Some(r) => r
      case None => js.undefined
    })
  }
  
  private def createUiParams(stacks: WithStacksProps): Option[ArchiverPluginUiParams] = {
    val stackItem = WithStacksProps.active(stacks).stack.peek[FileListState]()
    stackItem.getData().toOption.flatMap { case FileListData(dispatch, actions, state) =>
      val items =
        if (state.selectedNames.nonEmpty) FileListState.selectedItems(state).toList
        else FileListState.currentItem(state).filter(_ != FileListItem.up).toList
      
      if (actions.api.isLocal && items.nonEmpty) {
        val zipName = s"${items.head.name}.zip"
        val params = ArchiverPluginUiParams(FileListData(dispatch, actions, state), zipName, "zip", ZipApi.addToZip _, js.Array(items: _*))
        Some(params)
      }
      else None
    }
  }
  
  override def onFileTrigger(filePath: String,
                             fileHeader: Uint8Array,
                             onClose: js.Function0[Unit]): js.Promise[js.UndefOr[PanelStackItem[FileListState]]] = {
    val pathLower = filePath.toLowerCase
    val res =
      if (pathLower.endsWith(".zip") || pathLower.endsWith(".jar") || checkFileHeader(fileHeader)) {
        val fileName = path.parse(filePath).base
        val rootPath = s"zip://$fileName"
        val entriesByParentF = readZip(filePath)
        
        Some(PanelStackItem[FileListState](
          component = FileListPanelController(new ZipPanel(filePath, rootPath, entriesByParentF, onClose).apply()),
          dispatch = js.undefined,
          actions = new ZipActions(createApi(filePath, rootPath, entriesByParentF)),
          state = FileListState(
            currDir = FileListDir(rootPath, isRoot = false, items = js.Array())
          )
        ))
      }
      else None

    js.Promise.resolve[js.UndefOr[PanelStackItem[FileListState]]](res match {
      case Some(r) => r
      case None => js.undefined
    })
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
