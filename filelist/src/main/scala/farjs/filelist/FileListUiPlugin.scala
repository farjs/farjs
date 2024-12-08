package farjs.filelist

import farjs.filelist.api.{FileListCapability, FileListItem}
import farjs.filelist.stack.WithStacksProps
import scommons.react.ReactClass

import scala.scalajs.js

object FileListUiPlugin extends FileListPlugin(js.Array(
  "f1", "f7", "f8", "delete", "f9", "f10", "M-s", "M-d"
)) {

  override def onKeyTrigger(key: String,
                            stacks: WithStacksProps,
                            data: js.UndefOr[js.Dynamic] = js.undefined): js.Promise[js.UndefOr[ReactClass]] = {
    val maybeCurrData = {
      val stackItem = WithStacksProps.active(stacks).stack.peek[FileListState]()
      stackItem.getData().toOption
    }
    val res = createUiData(key, maybeCurrData).map(uiData => new FileListUi(uiData).apply())
    js.Promise.resolve[js.UndefOr[ReactClass]](res match {
      case Some(r) => r
      case None => js.undefined
    })
  }

  private[filelist] final def createUiData(key: String, data: Option[FileListData]): Option[FileListUiData] = {
    key match {
      case "f1" => Some(FileListUiData(showHelpPopup = true, data = data))
      case "f7" => data.flatMap {
        case d if d.actions.api.capabilities.contains(FileListCapability.mkDirs) =>
          Some(FileListUiData(showMkFolderPopup = true, data = data))
        case _ => None
      }
      case "f8" | "delete" => data.flatMap {
        case d if d.actions.api.capabilities.contains(FileListCapability.delete) &&
          (FileListState.selectedItems(d.state).nonEmpty || FileListState.currentItem(d.state).exists(_ != FileListItem.up)) =>
          Some(FileListUiData(showDeletePopup = true, data = data))
        case _ => None
      }
      case "f9" => Some(FileListUiData(showMenuPopup = true, data = data))
      case "f10" => Some(FileListUiData(showExitPopup = true, data = data))
      case "M-s" => Some(FileListUiData(showSelectPopup = Some(true), data = data))
      case "M-d" => Some(FileListUiData(showSelectPopup = Some(false), data = data))
      case _ => None
    }
  }
}
