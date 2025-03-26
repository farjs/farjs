package farjs.copymove

import farjs.filelist.api.FileListItem

import scala.scalajs.js

sealed trait CopyProcessItem extends js.Object {
  val item: FileListItem
  val toName: String
}

object CopyProcessItem {

  def apply(item: FileListItem,
            toName: String): CopyProcessItem = {

    js.Dynamic.literal(
      item = item,
      toName = toName
    ).asInstanceOf[CopyProcessItem]
  }

  def unapply(arg: CopyProcessItem): Option[(FileListItem, String)] = {
    Some((
      arg.item,
      arg.toName
    ))
  }
}
