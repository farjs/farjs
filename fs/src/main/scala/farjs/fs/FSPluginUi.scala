package farjs.fs

import farjs.filelist.stack.WithPanelStacks
import farjs.filelist.{FileListData, FileListPluginUiProps, FileListState}
import farjs.fs.FSPluginUi._
import farjs.fs.popups._
import scommons.react._

object FSPluginUi {

  private[fs] var drive: UiComponent[DriveControllerProps] = DriveController
  private[fs] var foldersHistory: UiComponent[FoldersHistoryControllerProps] = FoldersHistoryController
  private[fs] var folderShortcuts: UiComponent[FolderShortcutsControllerProps] = FolderShortcutsController

  def unapply(arg: FSPluginUi): Option[(Option[Boolean], Boolean, Boolean)] = {
    Some((
      arg.showDrivePopupOnLeft,
      arg.showFoldersHistoryPopup,
      arg.showFolderShortcutsPopup
    ))
  }
}

class FSPluginUi(val showDrivePopupOnLeft: Option[Boolean] = None,
                 val showFoldersHistoryPopup: Boolean = false,
                 val showFolderShortcutsPopup: Boolean = false
                ) extends FunctionComponent[FileListPluginUiProps] {

  protected def render(compProps: Props): ReactElement = {
    val stacks = WithPanelStacks.usePanelStacks
    val props = compProps.plain

    def onChangeDir(isLeft: Boolean)(dir: String): Unit = {
      val currStack =
        if (isLeft) stacks.left.stack
        else stacks.right.stack

      if (currStack.peek != currStack.peekLast) {
        currStack.clear()
      }

      val stackItem = currStack.peekLast[FileListState]
      stackItem.getData().foreach { case FileListData(dispatch, actions, state) =>
        if (dir != state.currDir.path) {
          dispatch(actions.changeDir(
            dispatch = dispatch,
            path = "",
            dir = dir
          ))
        }
      }
    }
    
    val onChangeDirInActivePanel: String => Unit =
      onChangeDir(stacks.left.stack.isActive)

    <.>()(
      <(drive())(^.wrapped := DriveControllerProps(
        dispatch = props.dispatch,
        showDrivePopupOnLeft = showDrivePopupOnLeft,
        onChangeDir = { (dir, isLeft) =>
          onChangeDir(isLeft)(dir)
        },
        onClose = props.onClose
      ))(),
      
      <(foldersHistory())(^.wrapped := FoldersHistoryControllerProps(
        showPopup = showFoldersHistoryPopup,
        onChangeDir = onChangeDirInActivePanel,
        onClose = props.onClose
      ))(),
      
      <(folderShortcuts())(^.wrapped := FolderShortcutsControllerProps(
        showPopup = showFolderShortcutsPopup,
        onChangeDir = onChangeDirInActivePanel,
        onClose = props.onClose
      ))()
    )
  }
}
