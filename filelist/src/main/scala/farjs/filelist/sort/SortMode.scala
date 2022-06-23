package farjs.filelist.sort

sealed trait SortMode

object SortMode {
  
  case object Name extends SortMode
  case object Extension extends SortMode
  case object ModificationTime extends SortMode
  case object Size extends SortMode
  case object Unsorted extends SortMode
  case object CreationTime extends SortMode
  case object AccessTime extends SortMode
}
