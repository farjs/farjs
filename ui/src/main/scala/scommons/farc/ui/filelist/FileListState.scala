package scommons.farc.ui.filelist

import scommons.farc.api.filelist._
import scommons.farc.ui.filelist.FileListActions._

case class FileListState(offset: Int = 0,
                         index: Int = 0,
                         currDir: FileListDir = FileListDir("", isRoot = false, Seq.empty),
                         selectedNames: Set[String] = Set.empty,
                         isRight: Boolean = false,
                         isActive: Boolean = false) {

  def currentItem: Option[FileListItem] = {
    val itemIndex = offset + index
    if (itemIndex >= 0 && itemIndex < currDir.items.size) {
      Some(currDir.items(itemIndex))
    }
    else None
  }
}

trait FileListsStateDef {

  def left: FileListState
  def right: FileListState
}

case class FileListsState(left: FileListState = FileListState(isActive = true),
                          right: FileListState = FileListState(isRight = true)
                         ) extends FileListsStateDef

object FileListsStateReducer {

  def apply(state: Option[FileListsState], action: Any): FileListsState = {
    reduce(state.getOrElse(FileListsState()), action)
  }

  private def reduce(state: FileListsState, action: Any): FileListsState = {

    def withState(isRight: Boolean)(copyState: FileListState => FileListState): FileListsState = {
      if (isRight) state.copy(right = copyState(state.right))
      else state.copy(left = copyState(state.left))
    }
    
    action match {
      case FileListParamsChangedAction(isRight, isActive, offset, index, selectedNames) =>
        withState(isRight)(_.copy(
          offset = offset,
          index = index,
          selectedNames = selectedNames,
          isActive = isActive
        ))
      case FileListDirChangedAction(isRight, dir, currDir) =>
        val items = {
          val sorted = currDir.items.sortBy(item => (!item.isDir, item.name))

          if (currDir.isRoot) sorted
          else FileListItem.up +: sorted
        }

        withState(isRight) { state =>
          val index =
            if (dir == FileListItem.up.name) {
              val focusedDir = state.currDir.path
                .stripPrefix(currDir.path)
                .stripPrefix("/")
                .stripPrefix("\\")

              math.max(items.indexWhere(_.name == focusedDir), 0)
            }
            else 0

          state.copy(
            offset = 0,
            index = index,
            currDir = currDir.copy(items = items),
            selectedNames = Set.empty
          )
        }
      case _ => state
    }
  }
}
