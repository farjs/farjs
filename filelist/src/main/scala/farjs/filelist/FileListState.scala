package farjs.filelist

import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.sort.{FileListSort, SortMode}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
sealed trait FileListState extends js.Object {

  val offset: Int = js.native
  val index: Int = js.native
  val currDir: FileListDir = js.native
  val selectedNames: js.Set[String] = js.native
  val isActive: Boolean = js.native
  val diskSpace: js.UndefOr[Double] = js.native
  val sort: FileListSort = js.native
}

@js.native
@JSImport("@farjs/filelist/FileListState.mjs", JSImport.Default)
object NativeFileListState extends js.Function0[FileListState] {

  def apply(): FileListState = js.native

  def currentItem(s: FileListState): js.UndefOr[FileListItem] = js.native

  def selectedItems(s: FileListState): js.Array[FileListItem] = js.native

  def isFileListState(s: js.Any): Boolean = js.native
}

object FileListState {

  def currentItem(state: FileListState): js.UndefOr[FileListItem] =
    NativeFileListState.currentItem(state)

  def selectedItems(state: FileListState): js.Array[FileListItem] =
    NativeFileListState.selectedItems(state)

  def isFileListState(state: js.Any): Boolean =
    NativeFileListState.isFileListState(state)

  def apply(offset: Int = 0,
            index: Int = 0,
            currDir: FileListDir = FileListDir("", isRoot = false, js.Array()),
            selectedNames: js.Set[String] = js.Set(),
            isActive: Boolean = false,
            diskSpace: js.UndefOr[Double] = js.undefined,
            sort: FileListSort = FileListSort(SortMode.Name, asc = true)): FileListState = {

    FileListState.copy(NativeFileListState())(
      offset = offset,
      index = index,
      currDir = currDir,
      selectedNames = selectedNames,
      isActive = isActive,
      diskSpace = diskSpace,
      sort = sort
    )
  }
  
  def unapply(arg: FileListState): Option[(Int, Int, FileListDir, js.Set[String], Boolean, js.UndefOr[Double], FileListSort)] = {
    Some((
      arg.offset,
      arg.index,
      arg.currDir,
      arg.selectedNames,
      arg.isActive,
      arg.diskSpace,
      arg.sort
    ))
  }

  def copy(p: FileListState)(offset: Int = p.offset,
                             index: Int = p.index,
                             currDir: FileListDir = p.currDir,
                             selectedNames: js.Set[String] = p.selectedNames,
                             isActive: Boolean = p.isActive,
                             diskSpace: js.UndefOr[Double] = p.diskSpace,
                             sort: FileListSort = p.sort): FileListState = {

    val res = NativeFileListState()
    val dynRes = res.asInstanceOf[js.Dynamic]
    dynRes.offset = offset
    dynRes.index = index
    dynRes.currDir = currDir
    dynRes.selectedNames = selectedNames
    dynRes.isActive = isActive
    dynRes.diskSpace = diskSpace
    dynRes.sort = sort
    res
  }
}
