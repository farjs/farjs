package farjs.fs.popups

import farjs.fs.popups.FSPopupsActions._

case class FSPopupsState(showDrivePopup: DrivePopupShow = DrivePopupHidden,
                         showFoldersHistoryPopup: Boolean = false,
                         showFolderShortcutsPopup: Boolean = false)

object FSPopupsStateReducer {

  def apply(state: Option[FSPopupsState], action: Any): FSPopupsState = {
    reduce(state.getOrElse(FSPopupsState()), action)
  }

  private def reduce(state: FSPopupsState, action: Any): FSPopupsState = action match {
    case DrivePopupAction(show) => state.copy(showDrivePopup = show)
    case FoldersHistoryPopupAction(show) => state.copy(showFoldersHistoryPopup = show)
    case FolderShortcutsPopupAction(show) => state.copy(showFolderShortcutsPopup = show)
    case _ => state
  }
}
