package farjs.filelist

import scommons.react.redux.Dispatch

case class FileListData(dispatch: Dispatch,
                        actions: FileListActions,
                        state: FileListState) {

  def path: String = state.currDir.path
}
