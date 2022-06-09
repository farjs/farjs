package farjs.filelist.popups

import farjs.filelist.FileListActions.FileListItemsViewedAction
import farjs.filelist.popups.FileListPopupsActions._

case class FileListPopupsState(showHelpPopup: Boolean = false,
                               showExitPopup: Boolean = false,
                               showDeletePopup: Boolean = false,
                               showMkFolderPopup: Boolean = false,
                               showViewItemsPopup: Boolean = false,
                               showCopyMovePopup: FileListPopupCopyMove = CopyMoveHidden,
                               showSelectPopup: FileListPopupSelect = SelectHidden)

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
    case FileListItemsViewedAction(_) => state.copy(showViewItemsPopup = false)
    case FileListPopupCopyMoveAction(cm) => state.copy(showCopyMovePopup = cm)
    case FileListPopupSelectAction(sel) => state.copy(showSelectPopup = sel)
    case _ => state
  }
}
