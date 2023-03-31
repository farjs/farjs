package farjs.filelist

import farjs.filelist.api.{FileListCapability, FileListItem}
import farjs.filelist.stack.WithPanelStacksProps
import scommons.react.ReactClass

import scala.scalajs.js

object FileListUiPlugin extends FileListPlugin {

  override val triggerKeys: js.Array[String] = js.Array(
    "f1", "f7", "f8", "delete", "f9", "f10", "M-s", "M-d"
  )

  override def onKeyTrigger(key: String, stacks: WithPanelStacksProps): Option[ReactClass] = {
    val maybeCurrData = {
      val stackItem = stacks.activeStack.peek[FileListState]
      stackItem.getActions.zip(stackItem.state).map { case ((dispatch, actions), state) =>
        FileListData(dispatch, actions, state)
      }
    }
    createUiData(key, maybeCurrData).map(uiData => new FileListUi(uiData).apply())
  }

  private[filelist] def createUiData(key: String, data: Option[FileListData]): Option[FileListUiData] = {
    key match {
      case "f1" => Some(FileListUiData(showHelpPopup = true, data = data))
      case "f7" => data.flatMap {
        case d if d.actions.capabilities.contains(FileListCapability.mkDirs) =>
          Some(FileListUiData(showMkFolderPopup = true, data = data))
        case _ => None
      }
      case "f8" | "delete" => data.flatMap {
        case d if d.actions.capabilities.contains(FileListCapability.delete) &&
          (d.state.selectedItems.nonEmpty || d.state.currentItem.exists(_ != FileListItem.up)) =>
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
