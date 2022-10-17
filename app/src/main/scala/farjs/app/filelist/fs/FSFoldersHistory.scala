package farjs.app.filelist.fs

import farjs.filelist.FileListServices
import scommons.react._
import scommons.react.hooks._

case class FSFoldersHistoryProps(showPopup: Boolean,
                                 currDirPath: String,
                                 onChangeDir: String => Unit,
                                 onHidePopup: () => Unit)

object FSFoldersHistory extends FunctionComponent[FSFoldersHistoryProps] {

  private[fs] var fsFoldersPopup: UiComponent[FSFoldersPopupProps] = FSFoldersPopup
  
  protected def render(compProps: Props): ReactElement = {
    val services = FileListServices.useServices
    val props = compProps.wrapped
    val currDirPath = props.currDirPath

    useLayoutEffect({ () =>
      if (currDirPath.nonEmpty) {
        services.foldersHistory.save(currDirPath)
      }
      ()
    }, List(currDirPath))

    if (props.showPopup) {
      <(fsFoldersPopup())(^.wrapped := FSFoldersPopupProps(
        onChangeDir = props.onChangeDir,
        onClose = props.onHidePopup
      ))()
    }
    else null
  }
}
