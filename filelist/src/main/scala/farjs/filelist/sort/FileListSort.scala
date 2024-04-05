package farjs.filelist.sort

import farjs.filelist.api.FileListItem

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

sealed trait FileListSort extends js.Object {
  val mode: SortMode
  val asc: Boolean
}

@js.native
@JSImport("@farjs/filelist/sort/FileListSort.mjs", JSImport.Default)
object NativeFileListSort extends js.Object {

  def nextSort(sort: FileListSort, nextMode: SortMode): FileListSort = js.native

  def sortItems(items: js.Array[FileListItem], mode: SortMode): js.Array[FileListItem] = js.native
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

  def nextSort(sort: FileListSort, nextMode: SortMode): FileListSort =
    NativeFileListSort.nextSort(sort, nextMode)

  def sortItems(items: js.Array[FileListItem], mode: SortMode): js.Array[FileListItem] =
    NativeFileListSort.sortItems(items, mode)
}
