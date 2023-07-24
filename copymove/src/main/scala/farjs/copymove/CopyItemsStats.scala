package farjs.copymove

import farjs.filelist.FileListActions
import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist.api.FileListItem
import farjs.ui.Dispatch
import farjs.ui.popup.{StatusPopup, StatusPopupProps}
import farjs.ui.task.FutureTask
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.{Failure, Success}

case class CopyItemsStatsProps(dispatch: Dispatch,
                               actions: FileListActions,
                               fromPath: String,
                               items: Seq[FileListItem],
                               title: String,
                               onDone: Double => Unit,
                               onCancel: () => Unit)

object CopyItemsStats extends FunctionComponent[CopyItemsStatsProps] {

  private[copymove] var statusPopupComp: UiComponent[StatusPopupProps] = StatusPopup

  protected def render(compProps: Props): ReactElement = {
    val (currDir, setCurrDir) = useState("")
    val inProgress = useRef(false)
    val props = compProps.wrapped

    def scanDir(): Unit = {
      val parent = props.fromPath
      var filesSize = 0.0
      val resultF = props.items.foldLeft(Future.successful(true)) { (resF, currItem) =>
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

    <(statusPopupComp())(^.plain := StatusPopupProps(
      text = s"Calculating total size\n$currDir",
      title = props.title,
      onClose = props.onCancel: js.Function0[Unit]
    ))()
  }
}
