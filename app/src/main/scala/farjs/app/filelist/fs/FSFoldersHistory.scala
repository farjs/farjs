package farjs.app.filelist.fs

import farjs.app.filelist.fs.popups.{FoldersHistoryPopup, FoldersHistoryPopupProps}
import farjs.filelist.FileListServices
import scommons.react._
import scommons.react.hooks._

case class FSFoldersHistoryProps(showPopup: Boolean,
                                 currDirPath: String,
                                 onChangeDir: String => Unit,
                                 onHidePopup: () => Unit)

object FSFoldersHistory extends FunctionComponent[FSFoldersHistoryProps] {

  private[fs] var foldersHistoryPopup: UiComponent[FoldersHistoryPopupProps] = FoldersHistoryPopup
  
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
      <(foldersHistoryPopup())(^.wrapped := FoldersHistoryPopupProps(
        onChangeDir = props.onChangeDir,
        onClose = props.onHidePopup
      ))()
    }
    else null
  }
}
