package farjs.filelist

import farjs.ui.Dispatch

import scala.scalajs.js

sealed trait FileListData extends js.Object {
  val dispatch: Dispatch
  val actions: FileListActions
  val state: FileListState
}

object FileListData {

  def apply(dispatch: Dispatch,
            actions: FileListActions,
            state: FileListState): FileListData = {

    js.Dynamic.literal(
      dispatch = dispatch,
      actions = actions,
      state = state
    ).asInstanceOf[FileListData]
  }

  def unapply(arg: FileListData): Option[(Dispatch, FileListActions, FileListState)] = {
    Some((
      arg.dispatch,
      arg.actions,
      arg.state
    ))
  }
}
