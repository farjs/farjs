package farjs.copymove

import farjs.filelist.api.FileListItem

import scala.scalajs.js

sealed trait FileExistsPopupProps extends js.Object {
  val newItem: FileListItem
  val existing: FileListItem
  val onAction: js.Function1[FileExistsAction, Unit]
  val onCancel: js.Function0[Unit]
}

object FileExistsPopupProps {

  def apply(newItem: FileListItem,
            existing: FileListItem,
            onAction: js.Function1[FileExistsAction, Unit],
            onCancel: js.Function0[Unit]): FileExistsPopupProps = {

    js.Dynamic.literal(
      newItem = newItem,
      existing = existing,
      onAction = onAction,
      onCancel = onCancel
    ).asInstanceOf[FileExistsPopupProps]
  }

  def unapply(arg: FileExistsPopupProps): Option[(FileListItem, FileListItem, js.Function1[FileExistsAction, Unit], js.Function0[Unit])] = {
    Some((
      arg.newItem,
      arg.existing,
      arg.onAction,
      arg.onCancel
    ))
  }
}
