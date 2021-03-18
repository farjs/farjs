package farjs.filelist.popups

import farjs.filelist.FileListActions.{FileListScanDirsAction, FileListItemsViewedAction}
import farjs.filelist.popups.FileListPopupsActions.FileListPopupViewItemsAction
import farjs.ui.popup._
import scommons.react._
import scommons.react.hooks._
import scommons.react.redux.task.FutureTask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object ViewItemsPopup extends FunctionComponent[FileListPopupsProps] {

  private[popups] var statusPopupComp: UiComponent[StatusPopupProps] = StatusPopup

  protected def render(compProps: Props): ReactElement = {
    val (currDir, setCurrDir) = useState("")
    val inProgress = useRef(false)
    val props = compProps.wrapped
    val showPopup = props.data.popups.showViewItemsPopup

    def viewItems(): Unit = {
      val state = props.data.activeList
      val parent = state.currDir.path
      val currItems =
        if (state.selectedItems.nonEmpty) state.selectedItems
        else state.currentItem.toList
      
      var sizes = currItems.map {
        case i if i.isDir => (i.name, 0d)
        case i => (i.name, i.size)
      }.toMap
      
      val resultF = currItems.foldLeft(Future.successful(true)) { (resF, currItem) =>
        resF.flatMap {
          case true if currItem.isDir =>
            setCurrDir(currItem.name)
            var s = 0d
            props.actions.scanDirs(parent, Seq(currItem), onNextDir = { (_, items) =>
              s += items.foldLeft(0d) { case (res, i) =>
                res + (if (i.isDir) 0d else i.size)
              }
              inProgress.current
            }).map { res =>
              sizes = sizes.updated(currItem.name, s + sizes.getOrElse(currItem.name, 0d))
              res
            }
          case res => Future.successful(res)
        }
      }
      resultF.onComplete {
        case Success(false) => // already cancelled
        case Success(true) => props.dispatch(FileListItemsViewedAction(state.isRight, sizes))
        case Failure(_) =>
          props.dispatch(FileListPopupViewItemsAction(show = false))
          props.dispatch(FileListScanDirsAction(FutureTask("Viewing Items", resultF)))
      }
    }

    useLayoutEffect({ () =>
      if (!inProgress.current && showPopup) { // start scan
        inProgress.current = true
        setCurrDir("")
        viewItems()
      } else if (inProgress.current && !showPopup) { // stop scan
        inProgress.current = false
      }
      ()
    }, List(showPopup))
    
    if (showPopup) {
      <(statusPopupComp())(^.wrapped := StatusPopupProps(
        text = s"Scanning the folder\n$currDir",
        title = "View",
        closable = true,
        onClose = { () =>
          props.dispatch(FileListPopupViewItemsAction(show = false))
        }
      ))()
    } else null
  }
}
