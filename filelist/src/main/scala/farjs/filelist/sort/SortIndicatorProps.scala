package farjs.filelist.sort

import scala.scalajs.js

sealed trait SortIndicatorProps extends js.Object {
  val sort: FileListSort
}

object SortIndicatorProps {

  def apply(sort: FileListSort): SortIndicatorProps = {
    js.Dynamic.literal(
      sort = sort
    ).asInstanceOf[SortIndicatorProps]
  }

  def unapply(arg: SortIndicatorProps): Option[FileListSort] = {
    Some(
      arg.sort
    )
  }
}
