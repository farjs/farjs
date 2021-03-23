package farjs.filelist.popups

import io.github.shogowada.scalajs.reactjs.redux.Action

object FileListPopupsActions {

  case class FileListPopupHelpAction(show: Boolean) extends Action
  case class FileListPopupExitAction(show: Boolean) extends Action
  case class FileListPopupDeleteAction(show: Boolean) extends Action
  case class FileListPopupMkFolderAction(show: Boolean) extends Action
  case class FileListPopupViewItemsAction(show: Boolean) extends Action
  case class FileListPopupCopyItemsAction(show: Boolean) extends Action
}
