package farjs.filelist.popups

import farjs.filelist.FileListData

case class PopupControllerProps(data: Option[FileListData],
                                popups: FileListPopupsState)
