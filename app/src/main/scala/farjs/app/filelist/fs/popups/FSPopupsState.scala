package farjs.app.filelist.fs.popups

import farjs.app.filelist.fs.popups.FSPopupsActions._

case class FSPopupsState(showFoldersHistoryPopup: Boolean = false,
                         showFolderShortcutsPopup: Boolean = false)

object FSPopupsStateReducer {

  def apply(state: Option[FSPopupsState], action: Any): FSPopupsState = {
    reduce(state.getOrElse(FSPopupsState()), action)
  }

  private def reduce(state: FSPopupsState, action: Any): FSPopupsState = action match {
    case FoldersHistoryPopupAction(show) => state.copy(showFoldersHistoryPopup = show)
    case FolderShortcutsPopupAction(show) => state.copy(showFolderShortcutsPopup = show)
    case _ => state
  }
}
