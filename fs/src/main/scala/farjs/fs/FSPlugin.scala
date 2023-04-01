package farjs.fs

import farjs.filelist._
import farjs.filelist.stack._
import farjs.ui.Dispatch
import scommons.react.ReactClass

import scala.scalajs.js

class FSPlugin(reducer: (FileListState, Any) => FileListState) extends FileListPlugin {

  override val triggerKeys: js.Array[String] = js.Array("M-l", "M-r", "M-h", "C-d")

  val component: ReactClass = new FileListPanelController(FSPanel).apply()

  def init(parentDispatch: Dispatch, stack: PanelStack): Unit = {
    stack.updateFor[FileListState](component) { item =>
      PanelStackItem.initDispatch(parentDispatch, reducer, stack, item).copy(
        actions = Some(FSFileListActions),
        state = Some(FileListState(isActive = stack.isActive))
      )
    }
  }

  override def onKeyTrigger(key: String, stacks: WithPanelStacksProps): Option[ReactClass] =
    createUi(key).map(_.apply())
  
  private[fs] def createUi(key: String): Option[FSPluginUi] = {
    key match {
      case "M-l" => Some(new FSPluginUi(showDrivePopupOnLeft = Some(true)))
      case "M-r" => Some(new FSPluginUi(showDrivePopupOnLeft = Some(false)))
      case "M-h" => Some(new FSPluginUi(showFoldersHistoryPopup = true))
      case "C-d" => Some(new FSPluginUi(showFolderShortcutsPopup = true))
      case _ => None
    }
  }
}

object FSPlugin extends FSPlugin(FileListStateReducer.apply)
