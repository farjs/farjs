package farjs.viewer

import farjs.filelist.FileListActions.FileListDirUpdatedAction
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.ui.Dispatch
import farjs.ui.popup._
import farjs.ui.task.{Task, TaskAction}
import farjs.viewer.ViewItemsPopup._
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.{Failure, Success}

object ViewItemsPopup {

  private[viewer] var statusPopupComp: ReactClass = StatusPopup
}

class ViewItemsPopup(data: FileListData) extends FunctionComponent[FileListPluginUiProps] {

  protected def render(compProps: Props): ReactElement = {
    val (currDir, setCurrDir) = useState("")
    val inProgress = useRef(true)
    val props = compProps.plain

    def viewItems(dispatch: Dispatch, actions: FileListActions, state: FileListState): Unit = {
      val parent = state.currDir.path
      val currSelected = FileListState.selectedItems(state).toList
      val currItems =
        if (currSelected.nonEmpty) currSelected
        else FileListState.currentItem(state).toList
      
      var sizes = currItems.map {
        case i if i.isDir => (i.name, 0d)
        case i => (i.name, i.size)
      }.toMap
      
      val resultF = currItems.foldLeft(Future.successful(true)) { (resF, currItem) =>
        resF.flatMap {
          case true if currItem.isDir =>
            setCurrDir(currItem.name)
            var s = 0d
            actions.scanDirs(parent, js.Array(currItem), onNextDir = { (_, items) =>
              s += items.foldLeft(0d) { case (res, i) =>
                res + (if (i.isDir) 0d else i.size)
              }
              inProgress.current
            }).toFuture.map { res =>
              sizes = sizes.updated(currItem.name, s + sizes.getOrElse(currItem.name, 0d))
              res
            }
          case res => Future.successful(res)
        }
      }
      resultF.onComplete { res =>
        props.onClose()
        res match {
          case Success(false) => // already cancelled
          case Success(true) =>
            val updatedItems = state.currDir.items.map { item =>
              sizes.get(item.name) match {
                case Some(size) => FileListItem.copy(item)(size = size)
                case None => item
              }
            }
            dispatch(FileListDirUpdatedAction(FileListDir.copy(state.currDir)(items = updatedItems)))
          case Failure(_) =>
            dispatch(TaskAction(Task("Viewing Items", resultF)))
        }
      }
    }

    useLayoutEffect({ () =>
      // start scan
      viewItems(data.dispatch, data.actions, data.state)
      ()
    }, Nil)
    
    <(statusPopupComp)(^.plain := StatusPopupProps(
      text = s"Scanning the folder\n$currDir",
      title = "View",
      onClose = { () =>
        // stop scan
        inProgress.current = false
      }: js.Function0[Unit]
    ))()
  }
}
