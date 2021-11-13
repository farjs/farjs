package farjs.filelist.popups

import scommons.react.redux.Action

object FileListPopupsActions {

  case class FileListPopupHelpAction(show: Boolean) extends Action
  case class FileListPopupExitAction(show: Boolean) extends Action
  case class FileListPopupDeleteAction(show: Boolean) extends Action
  case class FileListPopupMkFolderAction(show: Boolean) extends Action
  case class FileListPopupViewItemsAction(show: Boolean) extends Action
  case class FileListPopupCopyMoveAction(cm: FileListPopupCopyMove) extends Action
  
  sealed trait FileListPopupCopyMove
  case object CopyMoveHidden extends FileListPopupCopyMove
  case object ShowCopyToTarget extends FileListPopupCopyMove
  case object ShowCopyToCurrent extends FileListPopupCopyMove
  case object ShowMoveToTarget extends FileListPopupCopyMove
  case object ShowMoveToCurrent extends FileListPopupCopyMove
}
