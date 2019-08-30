package scommons.farc.ui.filelist

import scommons.farc.api.filelist._
import scommons.farc.ui.filelist.FileListActions._
import scommons.farc.ui.filelist.popups._

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
  def popups: FileListPopupsState
}

case class FileListsState(left: FileListState = FileListState(isActive = true),
                          right: FileListState = FileListState(isRight = true),
                          popups: FileListPopupsState = FileListPopupsState()
                         ) extends FileListsStateDef

object FileListsStateReducer {

  def apply(state: Option[FileListsState], action: Any): FileListsState = {
    val newState = FileListsState(
      left = reduce(isRight = false, state.map(_.left).getOrElse(FileListState(isActive = true)), action),
      right = reduce(isRight = true, state.map(_.right).getOrElse(FileListState(isRight = true)), action),
      popups = FileListPopupsStateReducer(state.map(_.popups), action)
    )
    state match {
      case Some(currState) if currState == newState => currState
      case _ => newState
    }
  }

  private def reduce(isRight: Boolean, state: FileListState, action: Any): FileListState = action match {
    case FileListParamsChangedAction(`isRight`, isActive, offset, index, selectedNames) =>
      state.copy(
        offset = offset,
        index = index,
        selectedNames = selectedNames,
        isActive = isActive
      )
    case FileListDirChangedAction(`isRight`, dir, currDir) =>
      val items = {
        val sorted = currDir.items.sortBy(item => (!item.isDir, item.name))

        if (currDir.isRoot) sorted
        else FileListItem.up +: sorted
      }

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
    case _ => state
  }
}
