package farjs.viewer.quickview

import farjs.filelist.api.FileListItem
import farjs.filelist.stack.PanelStack
import farjs.filelist.{FileListActions, FileListState}
import farjs.ui.Dispatch

import scala.scalajs.js

sealed trait QuickViewDirProps extends js.Object {
  val dispatch: Dispatch
  val actions: FileListActions
  val state: FileListState
  val stack: PanelStack
  val width: Int
  val currItem: FileListItem
}

object QuickViewDirProps {

  def apply(dispatch: Dispatch,
            actions: FileListActions,
            state: FileListState,
            stack: PanelStack,
            width: Int,
            currItem: FileListItem): QuickViewDirProps = {

    js.Dynamic.literal(
      dispatch = dispatch,
      actions = actions,
      state = state,
      stack = stack,
      width = width,
      currItem = currItem
    ).asInstanceOf[QuickViewDirProps]
  }

  def unapply(arg: QuickViewDirProps): Option[(Dispatch, FileListActions, FileListState, PanelStack, Int, FileListItem)] = {
    Some((
      arg.dispatch,
      arg.actions,
      arg.state,
      arg.stack,
      arg.width,
      arg.currItem
    ))
  }
}
