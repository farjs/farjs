package farjs.filelist.sort

import farjs.filelist.api.FileListItem
import farjs.filelist.sort.SortMode._

import scala.scalajs.js

sealed trait FileListSort extends js.Object {
  val mode: SortMode
  val asc: Boolean
}

object FileListSort {

  def apply(mode: SortMode, asc: Boolean): FileListSort = {
    js.Dynamic.literal(
      mode = mode,
      asc = asc
    ).asInstanceOf[FileListSort]
  }

  def unapply(arg: FileListSort): Option[(SortMode, Boolean)] = {
    Some((
      arg.mode,
      arg.asc
    ))
  }

  def copy(p: FileListSort)(mode: SortMode = p.mode, asc: Boolean = p.asc): FileListSort = {
    FileListSort(
      mode = mode,
      asc = asc
    )
  }

  def nextOrdering(sort: FileListSort, nextMode: SortMode): Boolean = {
    if (sort.mode == nextMode) !sort.asc
    else {
      nextMode match {
        case Name | Extension | Unsorted => true
        case ModificationTime | Size | CreationTime | AccessTime => false
      }
    }
  }

  def sortItems(items: Seq[FileListItem], mode: SortMode): Seq[FileListItem] = {
    mode match {
      case Name => items.sortBy(item => (item.nameNormalized(), item.name))
      case Extension => items.sortBy(item => (item.extNormalized(), item.ext(), item.nameNormalized(), item.name))
      case ModificationTime => items.sortBy(item => (item.mtimeMs, item.nameNormalized(), item.name))
      case Size => items.sortBy(item => (item.size, item.nameNormalized(), item.name))
      case Unsorted => items
      case CreationTime => items.sortBy(item => (item.ctimeMs, item.nameNormalized(), item.name))
      case AccessTime => items.sortBy(item => (item.atimeMs, item.nameNormalized(), item.name))
    }
  }
}
