package farjs.copymove

import farjs.filelist.FileListActions
import farjs.filelist.FileListActions.FileListTaskAction
import farjs.filelist.api.FileListItem
import farjs.ui.Dispatch
import farjs.ui.popup._
import farjs.ui.task.Task
import farjs.ui.theme.Theme
import scommons.nodejs.{FS, path}
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.util.{Failure, Success}

case class MoveProcessProps(dispatch: Dispatch,
                            actions: FileListActions,
                            fromPath: String,
                            items: Seq[(FileListItem, String)],
                            toPath: String,
                            onTopItem: FileListItem => Unit,
                            onDone: () => Unit)

object MoveProcess extends FunctionComponent[MoveProcessProps] {

  private[copymove] var statusPopupComp: ReactClass = StatusPopup
  private[copymove] var messageBoxComp: ReactClass = MessageBox
  private[copymove] var fs: FS = scommons.nodejs.fs

  private case class MoveState(currItem: String = "",
                               existing: Option[String] = None)

  protected def render(compProps: Props): ReactElement = {
    val (state, setState) = useStateUpdater(() => MoveState())
    val inProgress = useRef(false)
    val existsPromise = useRef(Promise.successful[Boolean](true))
    val askWhenExists = useRef(true)
    val currTheme = Theme.useTheme()
    val props = compProps.wrapped

    def moveItems(): Unit = {
      val resultF = props.items.foldLeft(Future.successful(true)) { case (resF, (currItem, toName)) =>
        resF.flatMap {
          case true if inProgress.current =>
            setState(_.copy(currItem = currItem.name))
            val oldPath = path.join(props.fromPath, currItem.name)
            val newPath = path.join(props.toPath, toName)
            val exists = !currItem.isDir && fs.existsSync(newPath)

            if (exists && askWhenExists.current) {
              setState(_.copy(existing = Some(newPath)))
              existsPromise.current = Promise[Boolean]()
            }
            existsPromise.current.future.flatMap { overwrite =>
              if (!exists || overwrite) {
                fs.rename(oldPath, newPath).map { _ =>
                  props.onTopItem(currItem)
                  inProgress.current
                }
              } else Future.successful(inProgress.current)
            }
          case res => Future.successful(res)
        }
      }
      resultF.onComplete {
        case Success(_) => props.onDone()
        case Failure(_) =>
          props.onDone()
          props.dispatch(FileListTaskAction(Task("Moving items", resultF)))
      }
    }

    def onExistsAction(overwrite: Boolean,
                       all: Boolean = false,
                       cancel: Boolean = false): js.Function0[Unit] = { () =>
      
      setState(_.copy(existing = None))
      askWhenExists.current = !all
      inProgress.current = !cancel
      existsPromise.current.trySuccess(overwrite)
    }

    useLayoutEffect({ () =>
      // start
      inProgress.current = true
      moveItems()
      ()
    }, Nil)

    <.>()(
      <(statusPopupComp)(^.plain := StatusPopupProps(
        text = s"Moving item\n${state.currItem}",
        title = "Move",
        onClose = { () =>
          // stop
          inProgress.current = false
        }: js.Function0[Unit]
      ))(),

      state.existing.map { existing =>
        <(messageBoxComp)(^.plain := MessageBoxProps(
          title = "Warning",
          message = s"File already exists.\nDo you want to overwrite it's content?\n\n$existing",
          actions = js.Array(
            MessageBoxAction("Overwrite", onExistsAction(overwrite = true)),
            MessageBoxAction("All", onExistsAction(overwrite = true, all = true)),
            MessageBoxAction("Skip", onExistsAction(overwrite = false)),
            MessageBoxAction("Skip all", onExistsAction(overwrite = false, all = true)),
            MessageBoxAction(
              label = "Cancel",
              onAction = onExistsAction(overwrite = false, all = true, cancel = true),
              triggeredOnClose = true
            )
          ),
          style = currTheme.popup.error
        ))()
      }
    )
  }
}
