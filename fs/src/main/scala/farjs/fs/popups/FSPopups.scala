package farjs.fs.popups

import farjs.filelist.FileListState
import farjs.filelist.stack.WithPanelStacks
import scommons.react._
import scommons.react.redux.Dispatch

case class FSPopupsProps(dispatch: Dispatch,
                         popups: FSPopupsState)

object FSPopups extends FunctionComponent[FSPopupsProps] {

  private[popups] var drive: UiComponent[DriveControllerProps] = DriveController
  private[popups] var foldersHistory: UiComponent[FoldersHistoryControllerProps] = FoldersHistoryController
  private[popups] var folderShortcuts: UiComponent[FolderShortcutsControllerProps] = FolderShortcutsController

  protected def render(compProps: Props): ReactElement = {
    val stacks = WithPanelStacks.usePanelStacks
    val props = compProps.wrapped

    def onChangeDir(isLeft: Boolean)(dir: String): Unit = {
      val currStack =
        if (isLeft) stacks.leftStack
        else stacks.rightStack

      if (currStack.peek != currStack.peekLast) {
        currStack.clear()
      }

      val stackItem = currStack.peekLast[FileListState]
      stackItem.getActions.zip(stackItem.state).foreach { case ((dispatch, actions), state) =>
        if (dir != state.currDir.path) {
          dispatch(actions.changeDir(
            dispatch = dispatch,
            parent = None,
            dir = dir
          ))
        }
      }
    }
    
    val onChangeDirInActivePanel: String => Unit =
      onChangeDir(stacks.leftStack.isActive)

    <.>()(
      <(drive())(^.wrapped := DriveControllerProps(
        dispatch = props.dispatch,
        show = props.popups.showDrivePopup,
        onChangeDir = { (dir, isLeft) =>
          onChangeDir(isLeft)(dir)
        }
      ))(),
      
      <(foldersHistory())(^.wrapped := FoldersHistoryControllerProps(
        dispatch = props.dispatch,
        showPopup = props.popups.showFoldersHistoryPopup,
        onChangeDir = onChangeDirInActivePanel
      ))(),
      
      <(folderShortcuts())(^.wrapped := FolderShortcutsControllerProps(
        dispatch = props.dispatch,
        showPopup = props.popups.showFolderShortcutsPopup,
        onChangeDir = onChangeDirInActivePanel
      ))()
    )
  }
}
