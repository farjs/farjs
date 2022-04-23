package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.api.{FileListDir, FileListItem}

case class FileListState(offset: Int = 0,
                         index: Int = 0,
                         currDir: FileListDir = FileListDir("", isRoot = false, Seq.empty),
                         selectedNames: Set[String] = Set.empty,
                         isActive: Boolean = false,
                         diskSpace: Option[Double] = None) {

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
      val processed = processDir(currDir)
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
      val processed = processDir(currDir)
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
      val processed = processDir(currDir)
      state.copy(
        offset = 0,
        index = math.max(processed.items.indexWhere(_.name == name), 0),
        currDir = processed
      )
    case FileListItemsViewedAction(sizes) =>
      val updatedItems = state.currDir.items.map { item =>
        sizes.get(item.name) match {
          case Some(size) => item.copy(size = size)
          case None => item
        }
      }
      state.copy(
        currDir = state.currDir.copy(items = updatedItems)
      )
    case FileListDiskSpaceUpdatedAction(diskSpace) =>
      state.copy(
        diskSpace = Some(diskSpace)
      )
    case _ => state
  }
  
  private def processDir(currDir: FileListDir): FileListDir = {
    val items = {
      val sorted = currDir.items.sortBy(item => (!item.isDir, item.name))

      if (currDir.isRoot) sorted
      else FileListItem.up +: sorted
    }
    
    currDir.copy(items = items)
  }
}
