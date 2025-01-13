package farjs.filelist

import scala.scalajs.js

sealed trait FileListUiData extends js.Object {
  val onClose: js.Function0[Unit]
  val data: js.UndefOr[FileListData]
  val showHelpPopup: js.UndefOr[Boolean]
  val showExitPopup: js.UndefOr[Boolean]
  val showMenuPopup: js.UndefOr[Boolean]
  val showDeletePopup: js.UndefOr[Boolean]
  val showMkFolderPopup: js.UndefOr[Boolean]
  val showSelectPopup: js.UndefOr[Boolean]
}

object FileListUiData {

  def apply(onClose: js.Function0[Unit] = () => (),
            data: js.UndefOr[FileListData] = js.undefined,
            showHelpPopup: js.UndefOr[Boolean] = js.undefined,
            showExitPopup: js.UndefOr[Boolean] = js.undefined,
            showMenuPopup: js.UndefOr[Boolean] = js.undefined,
            showDeletePopup: js.UndefOr[Boolean] = js.undefined,
            showMkFolderPopup: js.UndefOr[Boolean] = js.undefined,
            showSelectPopup: js.UndefOr[Boolean] = js.undefined): FileListUiData = {

    js.Dynamic.literal(
      onClose = onClose,
      data = data,
      showHelpPopup = showHelpPopup,
      showExitPopup = showExitPopup,
      showMenuPopup = showMenuPopup,
      showDeletePopup = showDeletePopup,
      showMkFolderPopup = showMkFolderPopup,
      showSelectPopup = showSelectPopup
    ).asInstanceOf[FileListUiData]
  }

  def unapply(arg: FileListUiData): Option[
    (js.Function0[Unit], js.UndefOr[FileListData], js.UndefOr[Boolean], js.UndefOr[Boolean], js.UndefOr[Boolean], js.UndefOr[Boolean], js.UndefOr[Boolean], js.UndefOr[Boolean])
  ] = {
    Some((
      arg.onClose,
      arg.data,
      arg.showHelpPopup,
      arg.showExitPopup,
      arg.showMenuPopup,
      arg.showDeletePopup,
      arg.showMkFolderPopup,
      arg.showSelectPopup
    ))
  }

  def copy(p: FileListUiData)(onClose: js.Function0[Unit] = p.onClose,
                              data: js.UndefOr[FileListData] = p.data,
                              showHelpPopup: js.UndefOr[Boolean] = p.showHelpPopup,
                              showExitPopup: js.UndefOr[Boolean] = p.showExitPopup,
                              showMenuPopup: js.UndefOr[Boolean] = p.showMenuPopup,
                              showDeletePopup: js.UndefOr[Boolean] = p.showDeletePopup,
                              showMkFolderPopup: js.UndefOr[Boolean] = p.showMkFolderPopup,
                              showSelectPopup: js.UndefOr[Boolean] = p.showSelectPopup): FileListUiData = {

    FileListUiData(
      onClose = onClose,
      data = data,
      showHelpPopup = showHelpPopup,
      showExitPopup = showExitPopup,
      showMenuPopup = showMenuPopup,
      showDeletePopup = showDeletePopup,
      showMkFolderPopup = showMkFolderPopup,
      showSelectPopup = showSelectPopup
    )
  }
}
