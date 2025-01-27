package farjs.filelist

import farjs.filelist.FileListUi._
import farjs.filelist.popups._
import scommons.react._

object FileListUi {

  private[filelist] var helpController: ReactClass = HelpController
  private[filelist] var exitController: ReactClass = ExitController
  private[filelist] var menuController: ReactClass = MenuController
  private[filelist] var deleteController: ReactClass = DeleteController
  private[filelist] var makeFolderController: ReactClass = MakeFolderController
  private[filelist] var selectController: ReactClass = SelectController
}

class FileListUi(val data: FileListUiData) extends FunctionComponent[FileListPluginUiProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val uiData = FileListUiData.copy(data)(onClose = props.onClose)

    <.>()(
      <(helpController)(^.plain := uiData)(),
      <(exitController)(^.plain := uiData)(),
      <(menuController)(^.plain := uiData)(),
      <(deleteController)(^.plain := uiData)(),
      <(makeFolderController)(^.plain := uiData)(),
      <(selectController)(^.plain := uiData)()
    )
  }
}
