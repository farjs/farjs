package scommons.farc.ui.filelist.popups

import scommons.farc.ui.filelist.popups.FileListPopupsActions._

case class FileListPopupsState(showHelpPopup: Boolean = false)

object FileListPopupsStateReducer {

  def apply(state: Option[FileListPopupsState], action: Any): FileListPopupsState = {
    reduce(state.getOrElse(FileListPopupsState()), action)
  }

  private def reduce(state: FileListPopupsState, action: Any): FileListPopupsState = action match {
    case FileListHelpAction(show) => state.copy(showHelpPopup = show)
    case _ => state
  }
}
