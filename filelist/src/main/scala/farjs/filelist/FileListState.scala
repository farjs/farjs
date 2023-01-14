package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.sort.SortMode

case class FileListState(offset: Int = 0,
                         index: Int = 0,
                         currDir: FileListDir = FileListDir("", isRoot = false, Seq.empty),
                         selectedNames: Set[String] = Set.empty,
                         isActive: Boolean = false,
                         diskSpace: Option[Double] = None,
                         sortMode: SortMode = SortMode.Name,
                         sortAscending: Boolean = true) {

  lazy val currentItem: Option[FileListItem] = {
    val itemIndex = offset + index
    if (itemIndex >= 0 && itemIndex < currDir.items.size) {
      Some(currDir.items(itemIndex))
    }
    else None
  }
  
  lazy val selectedItems: Seq[FileListItem] = {
    if (selectedNames.nonEmpty) {
      currDir.items.filter(i => selectedNames.contains(i.name))
    }
    else Nil
  }
}

object FileListStateReducer {
  
  def apply(state: FileListState, action: Any): FileListState = action match {
    case FileListParamsChangedAction(offset, index, selectedNames) =>
      state.copy(
        offset = offset,
        index = index,
        selectedNames = selectedNames
      )
    case FileListDirChangedAction(dir, currDir) =>
      val processed = processDir(currDir, state.sortMode, state.sortAscending)
      val index =
        if (dir == FileListItem.up.name) {
          val focusedDir = state.currDir.path
            .stripPrefix(currDir.path)
            .stripPrefix("/")
            .stripPrefix("\\")

          math.max(processed.items.indexWhere(_.name == focusedDir), 0)
        }
        else 0

      state.copy(
        offset = 0,
        index = index,
        currDir = processed,
        selectedNames = Set.empty
      )
    case FileListDirUpdatedAction(currDir) =>
      val processed = processDir(currDir, state.sortMode, state.sortAscending)
      val currIndex = state.offset + state.index
      val newIndex = state.currentItem.map { currItem =>
        val index = processed.items.indexWhere(_.name == currItem.name)
        if (index < 0) {
          math.min(currIndex, currDir.items.size)
        }
        else index
      }.getOrElse(0)
      
      val (offset, index) =
        if (newIndex == currIndex) (state.offset, state.index)
        else (0, newIndex)
      
      state.copy(
        offset = offset,
        index = index,
        currDir = processed,
        selectedNames =
          if (state.selectedNames.nonEmpty) {
            state.selectedNames.intersect(processed.items.map(_.name).toSet)
          }
          else state.selectedNames
      )
    case FileListItemCreatedAction(name, currDir) =>
      val processed = processDir(currDir, state.sortMode, state.sortAscending)
      val newIndex = processed.items.indexWhere(_.name == name)
      val (offset, index) =
        if (newIndex < 0) (state.offset, state.index)
        else (0, newIndex)

      state.copy(
        offset = offset,
        index = index,
        currDir = processed
      )
    case FileListSortByAction(mode) =>
      val ascending = SortMode.nextOrdering(state.sortMode, state.sortAscending, mode)
      val processed = processDir(state.currDir, mode, ascending)
      val newIndex = state.currentItem.map { item =>
        processed.items.indexWhere(_.name == item.name)
      }.getOrElse(-1)
      val (offset, index) =
        if (newIndex < 0) (state.offset, state.index)
        else (0, newIndex)

      state.copy(
        offset = offset,
        index = index,
        currDir = processed,
        sortMode = mode,
        sortAscending = ascending
      )
    case FileListDiskSpaceUpdatedAction(diskSpace) =>
      state.copy(
        diskSpace = Some(diskSpace)
      )
    case _ => state
  }
  
  private def processDir(currDir: FileListDir,
                         mode: SortMode,
                         ascending: Boolean): FileListDir = {
    val items = {
      val (dirs, files) = currDir.items.filter(_ != FileListItem.up).partition(_.isDir)
      val dirsSorted = sortItems(dirs, mode, ascending)
      val filesSorted = sortItems(files, mode, ascending)

      if (currDir.isRoot) dirsSorted :++ filesSorted
      else FileListItem.up +: dirsSorted :++ filesSorted
    }
    
    currDir.copy(items = items)
  }

  private def sortItems(items: Seq[FileListItem],
                        mode: SortMode,
                        ascending: Boolean): Seq[FileListItem] = {

    val sorted = SortMode.sortItems(items, mode)
    
    if (ascending) sorted
    else sorted.reverse
  }
}
