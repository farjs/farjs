package scommons.farc.ui.filelist

import scommons.farc.api.filelist._
import scommons.farc.ui.filelist.FileListActions._

case class FileListState(offset: Int = 0,
                         index: Int = 0,
                         currDir: FileListDir = FileListDir("", isRoot = false),
                         items: Seq[FileListItem] = Nil,
                         selectedNames: Set[String] = Set.empty,
                         isRight: Boolean = false) {

  def currentItem: Option[FileListItem] = {
    val itemIndex = offset + index
    if (itemIndex >= 0 && itemIndex < items.size) {
      Some(items(itemIndex))
    }
    else None
  }
}

trait FileListsStateDef {

  def left: FileListState
  def right: FileListState
}

case class FileListsState(left: FileListState = FileListState(),
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
      case FileListParamsChangedAction(isRight, offset, index, selectedNames) =>
        withState(isRight)(_.copy(
          offset = offset,
          index = index,
          selectedNames = selectedNames
        ))
      case FileListDirChangedAction(isRight, dir, currDir, files) =>
        val items = {
          val sorted = files.sortBy(item => (!item.isDir, item.name))

          if (currDir.isRoot) sorted
          else FileListItem.up +: sorted
        }

        withState(isRight) { state =>
          val index =
            if (dir.contains(FileListItem.up.name)) {
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
            currDir = currDir,
            items = items,
            selectedNames = Set.empty
          )
        }
      case _ => state
    }
  }
}
