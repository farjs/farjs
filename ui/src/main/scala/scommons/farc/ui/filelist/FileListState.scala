package scommons.farc.ui.filelist

case class FileListState(offset: Int = 0,
                         index: Int = 0,
                         selectedIds: Set[Int] = Set.empty)
