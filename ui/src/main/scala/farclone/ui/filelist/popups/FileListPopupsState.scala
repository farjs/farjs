package farclone.ui.filelist.popups

import farclone.ui.filelist.popups.FileListPopupsActions._

case class FileListPopupsState(showHelpPopup: Boolean = false,
                               showExitPopup: Boolean = false,
                               showDeletePopup: Boolean = false)

object FileListPopupsStateReducer {

  def apply(state: Option[FileListPopupsState], action: Any): FileListPopupsState = {
    reduce(state.getOrElse(FileListPopupsState()), action)
  }

  private def reduce(state: FileListPopupsState, action: Any): FileListPopupsState = action match {
    case FileListHelpAction(show) => state.copy(showHelpPopup = show)
    case FileListExitAction(show) => state.copy(showExitPopup = show)
    case FileListDeleteAction(show) => state.copy(showDeletePopup = show)
    case _ => state
  }
}
