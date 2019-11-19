package farclone.ui.filelist.popups

import io.github.shogowada.scalajs.reactjs.redux.Action

object FileListPopupsActions {

  case class FileListHelpAction(show: Boolean) extends Action
  case class FileListExitAction(show: Boolean) extends Action
  case class FileListDeleteAction(show: Boolean) extends Action
}
