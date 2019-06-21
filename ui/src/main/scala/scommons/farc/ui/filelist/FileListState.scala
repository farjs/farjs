package scommons.farc.ui.filelist

import scommons.farc.api.filelist._
import scommons.farc.ui.filelist.FileListActions._

case class FileListState(offset: Int = 0,
                         index: Int = 0,
                         currDir: FileListDir = FileListDir("", isRoot = false),
                         items: Seq[FileListItem] = Nil,
                         selectedNames: Set[String] = Set.empty) {

  def currentItem: Option[FileListItem] = {
    val itemIndex = offset + index
    if (itemIndex >= 0 && itemIndex < items.size) {
      Some(items(itemIndex))
    }
    else None
  }
}

object FileListStateReducer {

  def apply(state: Option[FileListState], action: Any): FileListState = {
    reduce(state.getOrElse(FileListState()), action)
  }

  private def reduce(state: FileListState, action: Any): FileListState = action match {
    case FileListParamsChangedAction(offset, index, selectedNames) => state.copy(
      offset = offset,
      index = index,
      selectedNames = selectedNames
    )
    case FileListDirChangedAction(dir, currDir, files) =>
      val items = {
        val sorted = files.sortBy(item => (!item.isDir, item.name))

        if (currDir.isRoot) sorted
        else FileListItem.up +: sorted
      }
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
    case _ => state
  }
}
