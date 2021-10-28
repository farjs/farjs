package farjs.filelist.copy

import farjs.filelist.FileListActions
import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist.api.FileListItem
import farjs.ui.popup.{StatusPopup, StatusPopupProps}
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.nodejs.{FS, path}
import scommons.react._
import scommons.react.hooks._
import scommons.react.redux.task.FutureTask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

case class MoveItemsProps(dispatch: Dispatch,
                          actions: FileListActions,
                          fromPath: String,
                          items: Seq[FileListItem],
                          toPath: String,
                          onTopItem: FileListItem => Unit,
                          onDone: () => Unit)

object MoveItems extends FunctionComponent[MoveItemsProps] {

  private[copy] var statusPopupComp: UiComponent[StatusPopupProps] = StatusPopup
  private[copy] var fs: FS = scommons.nodejs.fs

  protected def render(compProps: Props): ReactElement = {
    val (currItem, setCurrItem) = useState("")
    val inProgress = useRef(false)
    val props = compProps.wrapped

    def moveItems(): Unit = {
      val resultF = props.items.foldLeft(Future.successful(true)) { (resF, currItem) =>
        resF.flatMap {
          case true if inProgress.current =>
            setCurrItem(currItem.name)
            fs.rename(
              oldPath = path.join(props.fromPath, currItem.name),
              newPath = path.join(props.toPath, currItem.name)
            ).map { _ =>
              props.onTopItem(currItem)
              inProgress.current
            }
          case res => Future.successful(res)
        }
      }
      resultF.onComplete {
        case Success(_) => props.onDone()
        case Failure(_) =>
          props.onDone()
          props.dispatch(FileListTaskAction(FutureTask("Moving items", resultF)))
      }
    }

    useLayoutEffect({ () =>
      // start
      inProgress.current = true
      moveItems()
      ()
    }, Nil)

    <(statusPopupComp())(^.wrapped := StatusPopupProps(
      text = s"Moving item\n$currItem",
      title = "Move",
      closable = true,
      onClose = { () =>
        // stop
        inProgress.current = false
      }
    ))()
  }
}
