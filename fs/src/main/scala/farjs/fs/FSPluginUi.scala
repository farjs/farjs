package farjs.fs

import farjs.filelist.stack.WithStacks
import farjs.filelist.{FileListData, FileListPluginUiProps, FileListState}
import farjs.fs.FSPluginUi._
import farjs.fs.popups._
import scommons.react._

import scala.scalajs.js

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
    val stacks = WithStacks.useStacks()
    val props = compProps.plain

    def onChangeDir(isLeft: Boolean): js.Function1[String, Unit] = { dir =>
      val currStack =
        if (isLeft) stacks.left.stack
        else stacks.right.stack

      if (currStack.peek() != currStack.peekLast()) {
        currStack.clear()
      }

      val stackItem = currStack.peekLast[FileListState]()
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
    
    val onChangeDirInActivePanel: js.Function1[String, Unit] =
      onChangeDir(stacks.left.stack.isActive)

    <.>()(
      <(drive())(^.plain := DriveControllerProps(
        dispatch = props.dispatch,
        showDrivePopupOnLeft = showDrivePopupOnLeft match {
          case Some(v) => v
          case None => js.undefined
        },
        onChangeDir = { (dir, isLeft) =>
          onChangeDir(isLeft)(dir)
        },
        onClose = props.onClose
      ))(),
      
      <(foldersHistory())(^.plain := FoldersHistoryControllerProps(
        showPopup = showFoldersHistoryPopup,
        onChangeDir = onChangeDirInActivePanel,
        onClose = props.onClose
      ))(),
      
      <(folderShortcuts())(^.plain := FolderShortcutsControllerProps(
        showPopup = showFolderShortcutsPopup,
        onChangeDir = onChangeDirInActivePanel,
        onClose = props.onClose
      ))()
    )
  }
}
