package farjs.filelist.sort

import scala.scalajs.js

sealed trait SortModesPopupProps extends js.Object {
  val sort: FileListSort
  val onClose: js.Function0[Unit]
}

object SortModesPopupProps {

  def apply(sort: FileListSort, onClose: js.Function0[Unit]): SortModesPopupProps = {
    js.Dynamic.literal(
      sort = sort,
      onClose = onClose
    ).asInstanceOf[SortModesPopupProps]
  }

  def unapply(arg: SortModesPopupProps): Option[(FileListSort, js.Function0[Unit])] = {
    Some((
      arg.sort,
      arg.onClose,
    ))
  }
}
