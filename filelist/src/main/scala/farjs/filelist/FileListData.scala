package farjs.filelist

import farjs.ui.Dispatch

case class FileListData(dispatch: Dispatch,
                        actions: FileListActions,
                        state: FileListState) {

  def path: String = state.currDir.path
}
