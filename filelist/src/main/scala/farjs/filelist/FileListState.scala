package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.popups.{FileListPopupsState, FileListPopupsStateReducer}

case class FileListState(offset: Int = 0,
                         index: Int = 0,
                         currDir: FileListDir = FileListDir("", isRoot = false, Seq.empty),
                         selectedNames: Set[String] = Set.empty,
                         isRight: Boolean = false,
                         isActive: Boolean = false) {

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

trait FileListsStateDef {

  def left: FileListState
  def right: FileListState
  def popups: FileListPopupsState
  def activeList: FileListState
}

case class FileListsState(left: FileListState = FileListState(isActive = true),
                          right: FileListState = FileListState(isRight = true),
                          popups: FileListPopupsState = FileListPopupsState()
                         ) extends FileListsStateDef {
  
  lazy val activeList: FileListState = {
    if (left.isActive) left
    else right
  }
}

object FileListsStateReducer {

  def apply(state: Option[FileListsState], action: Any): FileListsState = {
    val newState = FileListsState(
      popups = FileListPopupsStateReducer(state.map(_.popups), action)
    )
    reduce(newState, action)
  }

  private def reduce(state: FileListsState, action: Any): FileListsState = action match {
    case FileListActivateAction(isRight) => state.copy(
      right = state.right.copy(isActive = isRight),
      left = state.left.copy(isActive = !isRight)
    )
    case _ => state
  }
  
  def reduceFileList(isRight: Boolean, state: FileListState, action: Any): FileListState = action match {
    case FileListParamsChangedAction(`isRight`, offset, index, selectedNames) =>
      state.copy(
        offset = offset,
        index = index,
        selectedNames = selectedNames
      )
    case FileListDirChangedAction(`isRight`, dir, currDir) =>
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
    case FileListDirUpdatedAction(`isRight`, currDir) =>
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
    case FileListDirCreatedAction(`isRight`, dir, currDir) =>
      val processed = processDir(currDir)
      state.copy(
        offset = 0,
        index = math.max(processed.items.indexWhere(_.name == dir), 0),
        currDir = processed
      )
    case FileListItemsViewedAction(`isRight`, sizes) =>
      val updatedItems = state.currDir.items.map { item =>
        sizes.get(item.name) match {
          case Some(size) => item.copy(size = size)
          case None => item
        }
      }
      state.copy(
        currDir = state.currDir.copy(items = updatedItems)
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
