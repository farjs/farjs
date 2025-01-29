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
      stackItem.getData()
    }
    val res = createUiData(key, maybeCurrData).map(uiData => FileListUi(uiData))
    js.Promise.resolve[js.UndefOr[ReactClass]](res)
  }

  private[filelist] final def createUiData(key: String, data: js.UndefOr[FileListData]): js.UndefOr[FileListUiData] = {
    key match {
      case "f1" => FileListUiData(showHelpPopup = true, data = data)
      case "f7" => data.flatMap {
        case d if d.actions.api.capabilities.contains(FileListCapability.mkDirs) =>
          FileListUiData(showMkFolderPopup = true, data = data)
        case _ => js.undefined
      }
      case "f8" | "delete" => data.flatMap {
        case d if d.actions.api.capabilities.contains(FileListCapability.delete) &&
          (FileListState.selectedItems(d.state).nonEmpty || FileListState.currentItem(d.state).exists(_ != FileListItem.up)) =>
          FileListUiData(showDeletePopup = true, data = data)
        case _ => js.undefined
      }
      case "f9" => FileListUiData(showMenuPopup = true, data = data)
      case "f10" => FileListUiData(showExitPopup = true, data = data)
      case "M-s" => FileListUiData(showSelectPopup = true, data = data)
      case "M-d" => FileListUiData(showSelectPopup = false, data = data)
      case _ => js.undefined
    }
  }
}
