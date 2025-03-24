package farjs.copymove

import farjs.filelist.FileListActions
import farjs.filelist.api.FileListItem
import farjs.ui.Dispatch

import scala.scalajs.js

sealed trait CopyItemsStatsProps extends js.Object {
  val dispatch: Dispatch
  val actions: FileListActions
  val fromPath: String
  val items: js.Array[FileListItem]
  val title: String
  val onDone: js.Function1[Double, Unit]
  val onCancel: js.Function0[Unit]
}

object CopyItemsStatsProps {

  def apply(dispatch: Dispatch,
            actions: FileListActions,
            fromPath: String,
            items: js.Array[FileListItem],
            title: String,
            onDone: js.Function1[Double, Unit],
            onCancel: js.Function0[Unit]): CopyItemsStatsProps = {

    js.Dynamic.literal(
      dispatch = dispatch,
      actions = actions,
      fromPath = fromPath,
      items = items,
      title = title,
      onDone = onDone,
      onCancel = onCancel
    ).asInstanceOf[CopyItemsStatsProps]
  }

  def unapply(arg: CopyItemsStatsProps): Option[(Dispatch, FileListActions, String, js.Array[FileListItem], String, js.Function1[Double, Unit], js.Function0[Unit])] = {
    Some((
      arg.dispatch,
      arg.actions,
      arg.fromPath,
      arg.items,
      arg.title,
      arg.onDone,
      arg.onCancel
    ))
  }
}
