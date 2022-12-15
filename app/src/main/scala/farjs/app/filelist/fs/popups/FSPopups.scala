package farjs.app.filelist.fs.popups

import farjs.filelist.FileListState
import farjs.filelist.stack.WithPanelStacks
import scommons.react._
import scommons.react.redux.Dispatch

case class FSPopupsProps(dispatch: Dispatch,
                         popups: FSPopupsState)

object FSPopups extends FunctionComponent[FSPopupsProps] {

  private[popups] var foldersHistory: UiComponent[FoldersHistoryControllerProps] = FoldersHistoryController
  private[popups] var folderShortcuts: UiComponent[FSPopupsProps] = FolderShortcutsController

  protected def render(compProps: Props): ReactElement = {
    val stacks = WithPanelStacks.usePanelStacks
    val props = compProps.wrapped

    def onChangeDir(dir: String): Unit = {
      val currStack = stacks.activeStack
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

    <.>()(
      <(foldersHistory())(^.wrapped := FoldersHistoryControllerProps(
        dispatch = props.dispatch,
        showPopup = props.popups.showFoldersHistoryPopup,
        onChangeDir = onChangeDir
      ))(),
      
      <(folderShortcuts())(^.wrapped := props)()
    )
  }
}
