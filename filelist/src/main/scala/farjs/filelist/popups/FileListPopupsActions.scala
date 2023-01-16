package farjs.filelist.popups

import scommons.react.redux.Action

object FileListPopupsActions {

  case class FileListPopupHelpAction(show: Boolean) extends Action
  case class FileListPopupExitAction(show: Boolean) extends Action
  case class FileListPopupMenuAction(show: Boolean) extends Action
  case class FileListPopupDeleteAction(show: Boolean) extends Action
  case class FileListPopupMkFolderAction(show: Boolean) extends Action
  case class FileListPopupSelectAction(sel: FileListPopupSelect) extends Action
  
  sealed trait FileListPopupSelect
  case object SelectHidden extends FileListPopupSelect
  case object ShowSelect extends FileListPopupSelect
  case object ShowDeselect extends FileListPopupSelect
}
