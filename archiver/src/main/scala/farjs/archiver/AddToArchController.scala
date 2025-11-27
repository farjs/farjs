package farjs.archiver

import farjs.archiver.zip.ZipApi
import farjs.filelist.FileListActions._
import farjs.ui.popup.{StatusPopup, StatusPopupProps}
import farjs.ui.task.{Task, TaskAction}
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.{Failure, Success}

object AddToArchController extends FunctionComponent[AddToArchControllerProps] {

  private[archiver] var addToArchPopup: ReactClass = AddToArchPopup
  private[archiver] var addToArchApi: (String, String, Set[String], () => Unit) => Future[Unit] =
    ZipApi.addToZip
  private[archiver] var statusPopupComp: ReactClass = StatusPopup

  protected def render(compProps: Props): ReactElement = {
    val (showAddPopup, setShowAddPopup) = useState(true)
    val (showStatusPopup, setShowStatusPopup) = useState(false)
    val (progress, setProgress) = useState(0)
    val props = compProps.plain
    
    val onAction: js.Function1[String, Unit] = { zipFile =>
      setShowAddPopup(false)
      setShowStatusPopup(true)

      val parent = props.state.currDir.path
      val currItems = props.items
      var totalItems = 0
      var addedItems = 0.0
      val resultF = for {
        _ <- props.actions.scanDirs(parent, currItems, onNextDir = { (_, items) =>
          totalItems += items.size
          true
        }).toFuture
        _ <- addToArchApi(zipFile, parent, currItems.map(_.name).toSet, { () =>
          addedItems += 1
          setProgress(math.min((addedItems / totalItems) * 100, 100).toInt)
        })
      } yield {
        if (props.state.selectedNames.nonEmpty) {
          props.dispatch(FileListParamsChangedAction(
            offset = props.state.offset,
            index = props.state.index,
            selectedNames = js.Set.empty
          ))
        }

        setShowStatusPopup(false)
        props.onComplete(zipFile)
      }
      resultF.onComplete {
        case Success(_) =>
        case Failure(_) =>
          setShowStatusPopup(false)
          props.dispatch(TaskAction(
            Task(s"${props.action} item(s) to zip archive", resultF)
          ))
      }
    }
    
    <.>()(
      if (showAddPopup) Some(
        <(addToArchPopup)(^.plain := AddToArchPopupProps(
          zipName = props.zipName,
          action = props.action,
          onAction = onAction,
          onCancel = props.onCancel
        ))()
      ) else None,

      if (showStatusPopup) Some(
        <(statusPopupComp)(^.plain := StatusPopupProps(
          text = s"${props.action} item(s) to zip archive\n$progress%"
        ))()
      ) else None
    )
  }
}
