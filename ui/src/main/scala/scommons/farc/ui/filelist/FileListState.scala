package scommons.farc.ui.filelist

import scommons.farc.api.filelist.FileListItem
import scommons.nodejs._

case class FileListState(offset: Int = 0,
                         index: Int = 0,
                         currDir: String = os.homedir(),
                         items: Seq[FileListItem] = Nil,
                         selectedNames: Set[String] = Set.empty)
