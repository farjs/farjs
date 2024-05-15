package farjs.fs

import farjs.filelist._
import farjs.filelist.stack._
import farjs.ui.Dispatch
import scommons.react.ReactClass

import scala.concurrent.Future
import scala.scalajs.js

class FSPlugin(reducer: js.Function2[FileListState, js.Any, FileListState]) extends FileListPlugin {

  override val triggerKeys: js.Array[String] = js.Array("M-l", "M-r", "M-h", "C-d")

  val component: ReactClass = new FileListPanelController(FSPanel).apply()

  def init(parentDispatch: Dispatch, stack: PanelStack): Unit = {
    stack.updateFor[FileListState](component) { item =>
      PanelStackItem.copy(initDispatch(parentDispatch, reducer, stack, item))(
        actions = FSFileListActions,
        state = FileListState(isActive = stack.isActive)
      )
    }
  }

  def initDispatch(parentDispatch: Dispatch,
                   reducer: js.Function2[FileListState, js.Any, FileListState],
                   stack: PanelStack,
                   item: PanelStackItem[FileListState]
                  ): PanelStackItem[FileListState] = {

    val dispatch: js.Function1[js.Any, Unit] = { action =>
      stack.updateFor[FileListState](item.component) { item =>
        item.updateState { state =>
          reducer(state, action)
        }
      }
      parentDispatch(action)
    }

    PanelStackItem.copy(item)(dispatch = dispatch)
  }

  override def onKeyTrigger(key: String,
                            stacks: PanelStacks,
                            data: js.UndefOr[js.Dynamic] = js.undefined): Future[Option[ReactClass]] = {

    Future.successful(createUi(key).map(_.apply()))
  }
  
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

object FSPlugin extends FSPlugin(FileListStateReducer)
