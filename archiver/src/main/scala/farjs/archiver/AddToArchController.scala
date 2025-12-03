package farjs.archiver

import farjs.filelist.FileListActions._
import farjs.ui.popup.{StatusPopup, StatusPopupProps}
import farjs.ui.task.{Task, TaskAction}
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.util.{Failure, Success}

object AddToArchController extends FunctionComponent[AddToArchControllerProps] {

  private[archiver] var addToArchPopup: ReactClass = AddToArchPopup
  private[archiver] var statusPopupComp: ReactClass = StatusPopup

  protected def render(compProps: Props): ReactElement = {
    val (showAddPopup, setShowAddPopup) = useState(true)
    val (showStatusPopup, setShowStatusPopup) = useState(false)
    val (progress, setProgress) = useState(0)
    val props = compProps.plain
    
    val onAction: js.Function1[String, Unit] = { archFile =>
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
        _ <- props.addToArchApi(archFile, parent, new js.Set(currItems.map(_.name)), { () =>
          addedItems += 1
          setProgress(math.min((addedItems / totalItems) * 100, 100).toInt)
        }).toFuture
      } yield {
        if (props.state.selectedNames.nonEmpty) {
          props.dispatch(FileListParamsChangedAction(
            offset = props.state.offset,
            index = props.state.index,
            selectedNames = js.Set.empty
          ))
        }

        setShowStatusPopup(false)
        props.onComplete(archFile)
      }
      resultF.onComplete {
        case Success(_) =>
        case Failure(_) =>
          setShowStatusPopup(false)
          props.dispatch(TaskAction(
            Task(s"${props.archAction} item(s) to ${props.archType} archive", resultF)
          ))
      }
    }
    
    <.>()(
      if (showAddPopup) Some(
        <(addToArchPopup)(^.plain := AddToArchPopupProps(
          archName = props.archName,
          archType = props.archType,
          action = props.archAction,
          onAction = onAction,
          onCancel = props.onCancel
        ))()
      ) else None,

      if (showStatusPopup) Some(
        <(statusPopupComp)(^.plain := StatusPopupProps(
          text = s"${props.archAction} item(s) to ${props.archType} archive\n$progress%"
        ))()
      ) else None
    )
  }
}
