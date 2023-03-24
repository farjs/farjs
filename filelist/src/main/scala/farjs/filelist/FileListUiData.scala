package farjs.filelist

import scala.scalajs.js

case class FileListUiData(showHelpPopup: Boolean = false,
                          showExitPopup: Boolean = false,
                          showMenuPopup: Boolean = false,
                          showDeletePopup: Boolean = false,
                          showMkFolderPopup: Boolean = false,
                          showSelectPopup: Option[Boolean] = None,
                          data: Option[FileListData] = None,
                          onClose: js.Function0[Unit] = () => ())
