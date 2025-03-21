package farjs.copymove

import farjs.filelist.api.FileListItem

import scala.scalajs.js

sealed trait CopyItemsPopupProps extends js.Object {
  val move: Boolean
  val path: String
  val items: js.Array[FileListItem]
  val onAction: js.Function1[String, Unit]
  val onCancel: js.Function0[Unit]
}

object CopyItemsPopupProps {

  def apply(move: Boolean,
            path: String,
            items: js.Array[FileListItem],
            onAction: js.Function1[String, Unit],
            onCancel: js.Function0[Unit]): CopyItemsPopupProps = {

    js.Dynamic.literal(
      move = move,
      path = path,
      items = items,
      onAction = onAction,
      onCancel = onCancel
    ).asInstanceOf[CopyItemsPopupProps]
  }

  def unapply(arg: CopyItemsPopupProps): Option[(Boolean, String, js.Array[FileListItem], js.Function1[String, Unit], js.Function0[Unit])] = {
    Some((
      arg.move,
      arg.path,
      arg.items,
      arg.onAction,
      arg.onCancel
    ))
  }
}
