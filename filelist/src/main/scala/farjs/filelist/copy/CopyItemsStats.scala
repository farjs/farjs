package farjs.filelist.copy

import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist.{FileListActions, FileListState}
import farjs.ui.popup.{StatusPopup, StatusPopupProps}
import scommons.react._
import scommons.react.hooks._
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.{Failure, Success}

case class CopyItemsStatsProps(dispatch: Dispatch,
                               actions: FileListActions,
                               state: FileListState,
                               title: String,
                               onDone: Double => Unit,
                               onCancel: () => Unit)

object CopyItemsStats extends FunctionComponent[CopyItemsStatsProps] {

  private[copy] var statusPopupComp: UiComponent[StatusPopupProps] = StatusPopup

  protected def render(compProps: Props): ReactElement = {
    val (currDir, setCurrDir) = useState("")
    val inProgress = useRef(false)
    val props = compProps.wrapped

    def scanDir(): Unit = {
      val parent = props.state.currDir.path
      val currItems =
        if (props.state.selectedItems.nonEmpty) props.state.selectedItems
        else props.state.currentItem.toList

      var filesSize = 0.0
      val resultF = currItems.foldLeft(Future.successful(true)) { (resF, currItem) =>
        resF.flatMap {
          case true if currItem.isDir && inProgress.current =>
            setCurrDir(currItem.name)
            props.actions.scanDirs(parent, Seq(currItem), onNextDir = { (_, items) =>
              filesSize += items.foldLeft(0d) { case (res, i) =>
                res + (if (i.isDir) 0d else i.size)
              }
              inProgress.current
            })
          case true if !currItem.isDir =>
            filesSize += currItem.size
            Future.successful(true)
          case res => Future.successful(res)
        }
      }
      resultF.onComplete {
        case Success(false) => // already cancelled
        case Success(true) =>
          props.onDone(filesSize)
        case Failure(_) =>
          props.onCancel()
          props.dispatch(FileListTaskAction(FutureTask(s"${props.title} dir scan", resultF)))
      }
    }

    useLayoutEffect({ () =>
      // start scan
      inProgress.current = true
      scanDir()
      
      val cleanup: js.Function0[Unit] = { () =>
        // stop scan
        inProgress.current = false
      }
      cleanup
    }, Nil)

    <(statusPopupComp())(^.wrapped := StatusPopupProps(
      text = s"Calculating total size\n$currDir",
      title = props.title,
      closable = true,
      onClose = props.onCancel
    ))()
  }
}
