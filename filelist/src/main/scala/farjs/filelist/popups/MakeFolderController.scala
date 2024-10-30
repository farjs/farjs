package farjs.filelist.popups

import farjs.filelist.history.{History, HistoryKind}
import farjs.filelist.{FileListServices, FileListUiData}
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object MakeFolderController extends FunctionComponent[FileListUiData] {
  
  val mkDirsHistoryKind: HistoryKind = HistoryKind("farjs.mkdirs", 50)

  private[popups] var makeFolderPopup: UiComponent[MakeFolderPopupProps] = MakeFolderPopup

  private var initialMultiple = false

  protected def render(compProps: Props): ReactElement = {
    val services = FileListServices.useServices
    val (multiple, setMultiple) = useState(initialMultiple)
    val props = compProps.wrapped

    props.data match {
      case Some(data) if props.showMkFolderPopup =>
        <(makeFolderPopup())(^.wrapped := MakeFolderPopupProps(
          multiple = multiple,
          onOk = { (dir, multiple) =>
            val action = data.actions.createDir(
              dispatch = data.dispatch,
              parent = data.state.currDir.path,
              dir = dir,
              multiple = multiple
            )
            for {
              _ <- action.task.result.toFuture
              mkDirsHistory <- services.historyProvider.get(mkDirsHistoryKind).toFuture
              _ <- mkDirsHistory.save(History(dir, js.undefined)).toFuture
            } yield {
              setMultiple(multiple)
              initialMultiple = multiple

              props.onClose()
            }
            data.dispatch(action)
          },
          onCancel = props.onClose
        ))()
      case _ => null
    }
  }
}
