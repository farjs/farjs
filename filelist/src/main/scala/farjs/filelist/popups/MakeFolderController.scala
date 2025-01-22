package farjs.filelist.popups

import farjs.filelist.FileListUiData
import farjs.filelist.history.{History, HistoryKind, HistoryProvider}
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object MakeFolderController extends FunctionComponent[FileListUiData] {
  
  val mkDirsHistoryKind: HistoryKind = HistoryKind("farjs.mkdirs", 50)

  private[popups] var makeFolderPopup: ReactClass = MakeFolderPopup

  private var initialMultiple = false

  protected def render(compProps: Props): ReactElement = {
    val historyProvider = HistoryProvider.useHistoryProvider()
    val (multiple, setMultiple) = useState(initialMultiple)
    val props = compProps.plain

    props.data.toOption match {
      case Some(data) if props.showMkFolderPopup.getOrElse(false) =>
        <(makeFolderPopup)(^.plain := MakeFolderPopupProps(
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
              mkDirsHistory <- historyProvider.get(mkDirsHistoryKind).toFuture
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
