package farclone.ui.filelist.popups

import io.github.shogowada.scalajs.reactjs.redux.Action

object FileListPopupsActions {

  case class FileListPopupHelpAction(show: Boolean) extends Action
  case class FileListPopupExitAction(show: Boolean) extends Action
  case class FileListPopupDeleteAction(show: Boolean) extends Action
}
