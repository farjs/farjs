package farjs.filelist.copy

import farjs.filelist.FileListActions
import farjs.filelist.FileListActions.FileListScanDirsAction
import farjs.filelist.api.FileListItem
import farjs.ui.popup._
import farjs.ui.theme.Theme
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.nodejs
import scommons.nodejs.raw.Timers
import scommons.react._
import scommons.react.hooks._
import scommons.react.redux.task.FutureTask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.util.{Failure, Success}

case class CopyProcessProps(dispatch: Dispatch,
                            actions: FileListActions,
                            fromPath: String,
                            items: Seq[FileListItem],
                            toPath: String,
                            total: Double,
                            onDone: () => Unit)

object CopyProcess extends FunctionComponent[CopyProcessProps] {

  private[copy] var copyProgressPopup: UiComponent[CopyProgressPopupProps] = CopyProgressPopup
  private[copy] var messageBoxComp: UiComponent[MessageBoxProps] = MessageBox
  
  private[copy] var timers: Timers = nodejs.global
  
  private case class CopyState(time100ms: Int = 0,
                               cancel: Boolean = false)

  private case class CopyData(item: String = "",
                              to: String = "",
                              itemPercent: Int = 0,
                              itemBytes: Double = 0.0,
                              total: Double = 0.0)

  protected def render(compProps: Props): ReactElement = {
    val (state, setState) = useStateUpdater(() => CopyState())
    val inProgress = useRef(false)
    val cancelPromise = useRef(Promise.successful(()))
    val data = useRef(CopyData())
    val props = compProps.wrapped
    
    def doCopy(): Unit = {

      def loop(parent: String, targetDirs: List[String], items: Seq[FileListItem]): Future[Boolean] = {
        items.foldLeft(Future.successful(true)) { (resF, item) =>
          resF.flatMap {
            case true if item.isDir && inProgress.current =>
              for {
                dirList <- props.actions.readDir(Some(parent), item.name)
                dstDirs = targetDirs :+ item.name
                _ <- props.actions.mkDirs(dstDirs)
                res <- loop(dirList.path, dstDirs, dirList.items)
              } yield res
            case true if !item.isDir && inProgress.current =>
              data.current = data.current.copy(
                item = item.name,
                to = nodejs.path.join(targetDirs :+ item.name: _*),
                itemPercent = 0,
                itemBytes = 0.0
              )
              props.actions.copyFile(List(parent), item, targetDirs, onExists = { _ =>
                Future.successful(None)
              }, onProgress = { (_, _, position) =>
                data.current = data.current.copy(
                  itemPercent = (divide(position, item.size) * 100).toInt,
                  itemBytes = position
                )
                cancelPromise.current.future.map(_ => inProgress.current)
              }).andThen {
                case Success(true) =>
                  val d = data.current
                  data.current = data.current.copy(
                    itemBytes = 0.0,
                    total = d.total + d.itemBytes
                  )
              }
            case res => Future.successful(res)
          }
        }
      }

      val resultF = loop(props.fromPath, List(props.toPath), props.items)
      resultF.onComplete {
        case Success(false) => // already cancelled
        case Success(true) => props.onDone()
        case Failure(_) =>
          props.onDone()
          props.dispatch(FileListScanDirsAction(FutureTask("Copying Items", resultF)))
      }
    }

    useLayoutEffect({ () =>
      val timerId = timers.setInterval({ () =>
        setState {
          case s if !s.cancel => s.copy(time100ms = s.time100ms + 1)
          case s => s
        }
      }, 100)
      
      inProgress.current = true
      doCopy()
      
      val cleanup: js.Function0[Unit] = { () =>
        inProgress.current = false
        timers.clearInterval(timerId)
      }
      cleanup
    }, Nil)

    val d = data.current
    val timeSeconds = state.time100ms / 10
    val bytesPerSecond = divide(d.total + d.itemBytes, timeSeconds)
    
    <.>()(
      <(copyProgressPopup())(^.wrapped := CopyProgressPopupProps(
        item = d.item,
        to = d.to,
        itemPercent = d.itemPercent,
        total = props.total,
        totalPercent = (divide(d.total + d.itemBytes, props.total) * 100).toInt,
        timeSeconds = timeSeconds,
        leftSeconds = divide(math.max(props.total - (d.total + d.itemBytes), 0.0), bytesPerSecond).toInt,
        bytesPerSecond = bytesPerSecond,
        onCancel = { () =>
          setState(_.copy(cancel = true))
          cancelPromise.current = Promise[Unit]()
        }
      ))(),

      if (state.cancel) Some {
        <(messageBoxComp())(^.wrapped := MessageBoxProps(
          title = "Operation has been interrupted",
          message = "Do you really want to cancel it?",
          actions = List(
            MessageBoxAction.YES { () =>
              cancelPromise.current.trySuccess(())
              props.onDone()
            },
            MessageBoxAction.NO { () =>
              cancelPromise.current.trySuccess(())
              setState(_.copy(cancel = false))
            }
          ),
          style = Theme.current.popup.error
        ))()
      }
      else None
    )
  }
  
  private def divide(x: Double, y: Double): Double = {
    if (y == 0.0) 0.0
    else x / y
  }
}
