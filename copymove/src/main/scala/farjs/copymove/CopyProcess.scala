package farjs.copymove

import farjs.filelist.FileListData
import farjs.filelist.api.FileListItem
import farjs.ui.popup._
import farjs.ui.task.{Task, TaskAction}
import farjs.ui.theme.Theme
import scommons.nodejs
import scommons.nodejs.raw.Timers
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.util.{Failure, Success}

case class CopyProcessProps(from: FileListData,
                            to: FileListData,
                            move: Boolean,
                            fromPath: String,
                            items: Seq[(FileListItem, String)],
                            toPath: String,
                            total: Double,
                            onTopItem: FileListItem => Unit,
                            onDone: () => Unit)

object CopyProcess extends FunctionComponent[CopyProcessProps] {

  private[copymove] var copyProgressPopup: UiComponent[CopyProgressPopupProps] = CopyProgressPopup
  private[copymove] var fileExistsPopup: UiComponent[FileExistsPopupProps] = FileExistsPopup
  private[copymove] var messageBoxComp: ReactClass = MessageBox
  
  private[copymove] var timers: Timers = nodejs.global
  
  private case class CopyState(time100ms: Int = 0,
                               cancel: Boolean = false,
                               existing: Option[FileListItem] = None)

  private case class CopyInfo(item: FileListItem = FileListItem(""),
                              to: String = "",
                              itemPercent: Int = 0,
                              itemBytes: Double = 0.0,
                              total: Double = 0.0,
                              askWhenExists: Boolean = true)

  protected def render(compProps: Props): ReactElement = {
    val (state, setState) = useStateUpdater(() => CopyState())
    val inProgress = useRef(false)
    val cancelPromise = useRef(Promise.successful(()))
    val existsPromise = useRef(Promise.successful[Option[Boolean]](None))
    val data = useRef(CopyInfo())
    val currTheme = Theme.useTheme()
    val props = compProps.wrapped
    
    def doCopy(): Unit = {

      def loop(copied: Boolean,
               parent: String,
               targetDirs: List[String],
               items: Seq[(FileListItem, String)]): Future[(Boolean, Boolean)] = {
        
        items.foldLeft(Future.successful((copied, inProgress.current))) { case (resF, (item, toName)) =>
          resF.flatMap {
            case (prevCopied, true) if item.isDir && inProgress.current =>
              for {
                dirList <- props.from.actions.readDir(Some(parent), item.name)
                dstDirs = targetDirs :+ toName
                _ <- props.to.actions.mkDirs(dstDirs)
                res <- loop(prevCopied, dirList.path, dstDirs, dirList.items.map(i => (i, i.name)).toSeq)
                (isCopied, done) = res
                _ <-
                  if (isCopied && done && props.move) props.from.actions.delete(parent, Seq(item))
                  else Future.unit
              } yield res
            case (prevCopied, true) if !item.isDir && inProgress.current =>
              data.current = data.current.copy(
                item = item,
                to = nodejs.path.join(targetDirs :+ toName: _*),
                itemPercent = 0,
                itemBytes = 0.0
              )
              var isCopied = true
              for {
                done <- props.from.actions.copyFile(
                  srcDirs = List(parent),
                  srcItem = item,
                  dstFileF = props.to.actions.writeFile(targetDirs, toName, onExists = { existing =>
                    if (inProgress.current && data.current.askWhenExists) {
                      setState(_.copy(existing = Some(existing)))
                      existsPromise.current = Promise[Option[Boolean]]()
                    }
                    existsPromise.current.future.map { maybeOverwrite =>
                      if (maybeOverwrite.isEmpty) {
                        isCopied = false
                      }
                      maybeOverwrite
                    }
                  }),
                  onProgress = { position =>
                    data.current = data.current.copy(
                      itemPercent = (divide(position, item.size) * 100).toInt,
                      itemBytes = position
                    )
                    cancelPromise.current.future.map(_ => inProgress.current)
                  }
                )
                _ <-
                  if (isCopied && done && props.move) props.from.actions.delete(parent, Seq(item))
                  else Future.unit
              } yield {
                if (done) {
                  val d = data.current
                  data.current = data.current.copy(
                    itemBytes = 0.0,
                    total = d.total + d.itemBytes
                  )
                }
                (prevCopied && isCopied, done)
              }
            case res => Future.successful(res)
          }
        }
      }

      val resultF = props.items.foldLeft(Future.successful(true)) { case (resF, topItem@(item, _)) =>
        resF.flatMap {
          case true if inProgress.current =>
            loop(copied = true, props.fromPath, List(props.toPath), Seq(topItem)).map { case (isCopied, done) =>
              if (isCopied && done) {
                props.onTopItem(item)
              }
              done
            }
          case res => Future.successful(res)
        }
      }
      resultF.onComplete {
        case Success(_) => props.onDone()
        case Failure(_) =>
          props.onDone()
          props.from.dispatch(TaskAction(Task("Copy/Move Items", resultF)))
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
        move = props.move,
        item = d.item.name,
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

      state.existing.map { existing =>
        <(fileExistsPopup())(^.wrapped := FileExistsPopupProps(
          newItem = d.item,
          existing = existing,
          onAction = { action =>
            setState(_.copy(existing = None))
            
            if (action == FileExistsAction.All || action == FileExistsAction.SkipAll) {
              data.current = data.current.copy(askWhenExists = false)
            }
            action match {
              case FileExistsAction.Overwrite | FileExistsAction.All =>
                existsPromise.current.trySuccess(Some(true))
              case FileExistsAction.Skip | FileExistsAction.SkipAll =>
                existsPromise.current.trySuccess(None)
              case FileExistsAction.Append =>
                existsPromise.current.trySuccess(Some(false))
            }
          },
          onCancel = { () =>
            setState(_.copy(existing = None))
            inProgress.current = false
            existsPromise.current.trySuccess(None)
          }
        ))()
      },

      if (state.cancel) Some {
        <(messageBoxComp)(^.plain := MessageBoxProps(
          title = "Operation has been interrupted",
          message = "Do you really want to cancel it?",
          actions = js.Array(
            MessageBoxAction.YES { () =>
              setState(_.copy(cancel = false))
              inProgress.current = false
              cancelPromise.current.trySuccess(())
            },
            MessageBoxAction.NO { () =>
              setState(_.copy(cancel = false))
              cancelPromise.current.trySuccess(())
            }
          ),
          style = currTheme.popup.error
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
