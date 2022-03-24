package farjs.filelist.copy

import farjs.filelist.{FileListActions, FileListState}
import scommons.react.redux.Dispatch

case class CopyData(dispatch: Dispatch,
                    actions: FileListActions,
                    state: FileListState) {

  def path: String = state.currDir.path
}
