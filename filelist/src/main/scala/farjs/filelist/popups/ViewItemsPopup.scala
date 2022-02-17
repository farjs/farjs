package farjs.filelist.popups

import farjs.filelist.FileListActions.{FileListItemsViewedAction, FileListTaskAction}
import farjs.filelist.{FileListActions, FileListState}
import farjs.filelist.popups.FileListPopupsActions.FileListPopupViewItemsAction
import farjs.filelist.stack.WithPanelStacks
import farjs.ui.popup._
import scommons.react._
import scommons.react.hooks._
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object ViewItemsPopup extends FunctionComponent[FileListPopupsState] {

  private[popups] var statusPopupComp: UiComponent[StatusPopupProps] = StatusPopup

  protected def render(compProps: Props): ReactElement = {
    val stacks = WithPanelStacks.usePanelStacks
    val (currDir, setCurrDir) = useState("")
    val inProgress = useRef(false)
    val showPopup = compProps.wrapped.showViewItemsPopup
    val maybeCurrData = {
      val stackItem = stacks.activeStack.peek[FileListState]
      stackItem.getActions.zip(stackItem.state)
    }

    def viewItems(dispatch: Dispatch, actions: FileListActions, state: FileListState): Unit = {
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
            actions.scanDirs(parent, Seq(currItem), onNextDir = { (_, items) =>
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
        case Success(true) => dispatch(FileListItemsViewedAction(sizes))
        case Failure(_) =>
          dispatch(FileListPopupViewItemsAction(show = false))
          dispatch(FileListTaskAction(FutureTask("Viewing Items", resultF)))
      }
    }

    useLayoutEffect({ () =>
      if (!inProgress.current && showPopup) { // start scan
        maybeCurrData.foreach { case ((dispatch, actions), state) =>
          inProgress.current = true
          setCurrDir("")
          viewItems(dispatch, actions, state)
        }
      } else if (inProgress.current && !showPopup) { // stop scan
        inProgress.current = false
      }
      ()
    }, List(showPopup))
    
    maybeCurrData.filter(_ => showPopup).map { case ((dispatch, _), _) =>
      <(statusPopupComp())(^.wrapped := StatusPopupProps(
        text = s"Scanning the folder\n$currDir",
        title = "View",
        closable = true,
        onClose = { () =>
          dispatch(FileListPopupViewItemsAction(show = false))
        }
      ))()
    }.orNull
  }
}
