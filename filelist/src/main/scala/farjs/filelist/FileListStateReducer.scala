package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.sort.FileListSort

import scala.scalajs.js

object FileListStateReducer {
  
  def apply(state: FileListState, action: Any): FileListState = {
    val actionName = action.asInstanceOf[js.Dynamic].action.asInstanceOf[js.UndefOr[String]]
    action match {
      case a if actionName.exists(_ == FileListParamsChangedAction.name) =>
        val FileListParamsChangedAction(offset, index, selectedNames) = a.asInstanceOf[FileListParamsChangedAction]
        FileListState.copy(state)(
          offset = offset,
          index = index,
          selectedNames = selectedNames
        )
      case a if actionName.exists(_ == FileListDirChangedAction.name) =>
        val FileListDirChangedAction(dir, currDir) = a.asInstanceOf[FileListDirChangedAction]
        val processed = processDir(currDir, state.sort)
        val index =
          if (dir == FileListItem.up.name) {
            val focusedDir = state.currDir.path
              .stripPrefix(currDir.path)
              .stripPrefix("/")
              .stripPrefix("\\")

            math.max(processed.items.indexWhere(_.name == focusedDir), 0)
          }
          else 0

        FileListState.copy(state)(
          offset = 0,
          index = index,
          currDir = processed,
          selectedNames = js.Set[String]()
        )
      case a if actionName.exists(_ == FileListDirUpdatedAction.name) =>
        val FileListDirUpdatedAction(currDir) = a.asInstanceOf[FileListDirUpdatedAction]
        val processed = processDir(currDir, state.sort)
        val currIndex = state.offset + state.index
        val newIndex = FileListState.currentItem(state).map { currItem =>
          val index = processed.items.indexWhere(_.name == currItem.name)
          if (index < 0) {
            math.min(currIndex, currDir.items.size)
          }
          else index
        }.getOrElse(0)

        val (offset, index) =
          if (newIndex == currIndex) (state.offset, state.index)
          else (0, newIndex)

        FileListState.copy(state)(
          offset = offset,
          index = index,
          currDir = processed,
          selectedNames =
            if (state.selectedNames.nonEmpty) {
              val res = state.selectedNames.toSet.intersect(processed.items.map(_.name).toSet)
              js.Set[String](res.toSeq: _*)
            }
            else state.selectedNames
        )
      case a if actionName.exists(_ == FileListItemCreatedAction.name) =>
        val FileListItemCreatedAction(name, currDir) = a.asInstanceOf[FileListItemCreatedAction]
        val processed = processDir(currDir, state.sort)
        val newIndex = processed.items.indexWhere(_.name == name)
        val (offset, index) =
          if (newIndex < 0) (state.offset, state.index)
          else (0, newIndex)

        FileListState.copy(state)(
          offset = offset,
          index = index,
          currDir = processed
        )
      case FileListSortAction(mode) =>
        val nextSort = FileListSort.nextSort(state.sort, mode)
        val processed = processDir(state.currDir, nextSort)
        val newIndex = FileListState.currentItem(state).map { item =>
          processed.items.indexWhere(_.name == item.name)
        }.getOrElse(-1)
        val (offset, index) =
          if (newIndex < 0) (state.offset, state.index)
          else (0, newIndex)

        FileListState.copy(state)(
          offset = offset,
          index = index,
          currDir = processed,
          sort = nextSort
        )
      case FileListDiskSpaceUpdatedAction(diskSpace) =>
        FileListState.copy(state)(
          diskSpace = diskSpace
        )
      case _ => state
    }
  }
  
  private def processDir(currDir: FileListDir,
                         sort: FileListSort): FileListDir = {
    val items = {
      val (dirs, files) = currDir.items.filter(_ != FileListItem.up).partition(_.isDir)
      val dirsSorted = sortItems(dirs, sort)
      val filesSorted = sortItems(files, sort)

      if (currDir.isRoot) dirsSorted :++ filesSorted
      else FileListItem.up +: dirsSorted :++ filesSorted
    }

    FileListDir.copy(currDir)(items = items)
  }

  private def sortItems(items: js.Array[FileListItem],
                        sort: FileListSort): js.Array[FileListItem] = {

    val sorted = FileListSort.sortItems(items, sort.mode)
    
    if (sort.asc) sorted
    else sorted.reverse
  }
}
