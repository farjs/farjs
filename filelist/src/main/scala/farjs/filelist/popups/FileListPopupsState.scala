package farjs.filelist.popups

import farjs.filelist.FileListActions.FileListItemsViewedAction
import farjs.filelist.popups.FileListPopupsActions._

case class FileListPopupsState(showHelpPopup: Boolean = false,
                               showExitPopup: Boolean = false,
                               showDeletePopup: Boolean = false,
                               showMkFolderPopup: Boolean = false,
                               showViewItemsPopup: Boolean = false,
                               showCopyItemsPopup: Boolean = false,
                               showMoveItemsPopup: Boolean = false)

object FileListPopupsStateReducer {

  def apply(state: Option[FileListPopupsState], action: Any): FileListPopupsState = {
    reduce(state.getOrElse(FileListPopupsState()), action)
  }

  private def reduce(state: FileListPopupsState, action: Any): FileListPopupsState = action match {
    case FileListPopupHelpAction(show) => state.copy(showHelpPopup = show)
    case FileListPopupExitAction(show) => state.copy(showExitPopup = show)
    case FileListPopupDeleteAction(show) => state.copy(showDeletePopup = show)
    case FileListPopupMkFolderAction(show) => state.copy(showMkFolderPopup = show)
    case FileListPopupViewItemsAction(show) => state.copy(showViewItemsPopup = show)
    case FileListItemsViewedAction(_, _) => state.copy(showViewItemsPopup = false)
    case FileListPopupCopyItemsAction(show) => state.copy(showCopyItemsPopup = show)
    case FileListPopupMoveItemsAction(show) => state.copy(showMoveItemsPopup = show)
    case _ => state
  }
}
