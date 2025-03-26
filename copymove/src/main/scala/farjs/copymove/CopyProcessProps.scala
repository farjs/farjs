package farjs.copymove

import farjs.filelist.FileListData
import farjs.filelist.api.FileListItem

import scala.scalajs.js

sealed trait CopyProcessProps extends js.Object {
  val from: FileListData
  val to: FileListData
  val move: Boolean
  val fromPath: String
  val items: js.Array[CopyProcessItem]
  val toPath: String
  val total: Double
  val onTopItem: js.Function1[FileListItem, Unit]
  val onDone: js.Function0[Unit]
}

object CopyProcessProps {

  def apply(from: FileListData,
            to: FileListData,
            move: Boolean,
            fromPath: String,
            items: js.Array[CopyProcessItem],
            toPath: String,
            total: Double,
            onTopItem: js.Function1[FileListItem, Unit],
            onDone: js.Function0[Unit]): CopyProcessProps = {

    js.Dynamic.literal(
      from = from,
      to = to,
      move = move,
      fromPath = fromPath,
      items = items,
      toPath = toPath,
      total = total,
      onTopItem = onTopItem,
      onDone = onDone
    ).asInstanceOf[CopyProcessProps]
  }

  def unapply(arg: CopyProcessProps): Option[(FileListData, FileListData, Boolean, String, js.Array[CopyProcessItem], String, Double, js.Function1[FileListItem, Unit], js.Function0[Unit])] = {
    Some((
      arg.from,
      arg.to,
      arg.move,
      arg.fromPath,
      arg.items,
      arg.toPath,
      arg.total,
      arg.onTopItem,
      arg.onDone
    ))
  }
}
