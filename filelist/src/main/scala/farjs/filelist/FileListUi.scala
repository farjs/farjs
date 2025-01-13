package farjs.filelist

import farjs.filelist.FileListUi._
import farjs.filelist.popups._
import scommons.react._

object FileListUi {

  private[filelist] var helpController: UiComponent[FileListUiData] = HelpController
  private[filelist] var exitController: UiComponent[FileListUiData] = ExitController
  private[filelist] var menuController: UiComponent[FileListUiData] = MenuController
  private[filelist] var deleteController: UiComponent[FileListUiData] = DeleteController
  private[filelist] var makeFolderController: UiComponent[FileListUiData] = MakeFolderController
  private[filelist] var selectController: UiComponent[FileListUiData] = SelectController
}

class FileListUi(val data: FileListUiData) extends FunctionComponent[FileListPluginUiProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val uiData = FileListUiData.copy(data)(onClose = props.onClose)

    <.>()(
      <(helpController())(^.plain := uiData)(),
      <(exitController())(^.plain := uiData)(),
      <(menuController())(^.plain := uiData)(),
      <(deleteController())(^.plain := uiData)(),
      <(makeFolderController())(^.plain := uiData)(),
      <(selectController())(^.plain := uiData)()
    )
  }
}
