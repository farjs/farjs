package farjs.filelist.sort

import farjs.filelist.api.FileListItem

sealed trait SortMode

object SortMode {
  
  case object Name extends SortMode
  case object Extension extends SortMode
  case object ModificationTime extends SortMode
  case object Size extends SortMode
  case object Unsorted extends SortMode
  case object CreationTime extends SortMode
  case object AccessTime extends SortMode

  def nextOrdering(mode: SortMode, ascending: Boolean, nextMode: SortMode): Boolean = {
    if (mode == nextMode) !ascending
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
