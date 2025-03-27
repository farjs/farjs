package farjs.copymove

import farjs.filelist.FileListActions
import farjs.filelist.api.FileListItem
import farjs.ui.Dispatch

import scala.scalajs.js

sealed trait MoveProcessProps extends js.Object {
  val dispatch: Dispatch
  val actions: FileListActions
  val fromPath: String
  val items: js.Array[CopyProcessItem]
  val toPath: String
  val onTopItem: js.Function1[FileListItem, Unit]
  val onDone: js.Function0[Unit]
}

object MoveProcessProps {

  def apply(dispatch: Dispatch,
            actions: FileListActions,
            fromPath: String,
            items: js.Array[CopyProcessItem],
            toPath: String,
            onTopItem: js.Function1[FileListItem, Unit],
            onDone: js.Function0[Unit]): MoveProcessProps = {

    js.Dynamic.literal(
      dispatch = dispatch,
      actions = actions,
      fromPath = fromPath,
      items = items,
      toPath = toPath,
      onTopItem = onTopItem,
      onDone = onDone
    ).asInstanceOf[MoveProcessProps]
  }

  def unapply(arg: MoveProcessProps): Option[(Dispatch, FileListActions, String, js.Array[CopyProcessItem], String, js.Function1[FileListItem, Unit], js.Function0[Unit])] = {
    Some((
      arg.dispatch,
      arg.actions,
      arg.fromPath,
      arg.items,
      arg.toPath,
      arg.onTopItem,
      arg.onDone
    ))
  }
}
