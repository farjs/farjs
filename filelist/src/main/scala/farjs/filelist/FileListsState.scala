package farjs.filelist

import farjs.filelist.popups.{FileListPopupsState, FileListPopupsStateReducer}

case class FileListsState(popups: FileListPopupsState = FileListPopupsState())

object FileListsStateReducer {

  def apply(state: Option[FileListsState], action: Any): FileListsState = {
    FileListsState(
      popups = FileListPopupsStateReducer(state.map(_.popups), action)
    )
  }
}
