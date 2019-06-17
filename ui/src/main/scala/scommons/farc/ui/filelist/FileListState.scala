package scommons.farc.ui.filelist

import scommons.farc.api.filelist.{FileListDir, FileListItem}

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
