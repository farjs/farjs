package farjs.file

import farjs.file.FilePluginUi.fileViewHistory
import farjs.file.popups._
import farjs.filelist.FileListPluginUiProps
import scommons.react._

class FilePluginUi(val showFileViewHistoryPopup: Boolean = false)
  extends FunctionComponent[FileListPluginUiProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain

    <.>()(
      <(fileViewHistory)(^.plain := FileViewHistoryControllerProps(
        showPopup = showFileViewHistoryPopup,
        onClose = props.onClose
      ))()
    )
  }
}

object FilePluginUi {

  private[file] var fileViewHistory: ReactClass =
    FileViewHistoryController

  def unapply(arg: FilePluginUi): Option[Boolean] = {
    Some(
      arg.showFileViewHistoryPopup
    )
  }
}
