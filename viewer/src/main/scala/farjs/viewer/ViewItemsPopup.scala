package farjs.viewer

import farjs.filelist.FileListActions.{FileListDirUpdatedAction, FileListTaskAction}
import farjs.filelist.stack.WithPanelStacks
import farjs.filelist.{FileListActions, FileListPluginUiProps, FileListState}
import farjs.ui.popup._
import scommons.react._
import scommons.react.hooks._
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object ViewItemsPopup extends FunctionComponent[FileListPluginUiProps] {

  private[viewer] var statusPopupComp: UiComponent[StatusPopupProps] = StatusPopup

  protected def render(compProps: Props): ReactElement = {
    val stacks = WithPanelStacks.usePanelStacks
    val (currDir, setCurrDir) = useState("")
    val inProgress = useRef(false)
    val props = compProps.plain
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
      resultF.onComplete { res =>
        props.onClose()
        res match {
          case Success(false) => // already cancelled
          case Success(true) =>
            dispatch(FileListDirUpdatedAction(state.currDir.copy(items = state.currDir.items.map { item =>
              sizes.get(item.name) match {
                case Some(size) => item.copy(size = size)
                case None => item
              }
            })))
          case Failure(_) =>
            dispatch(FileListTaskAction(FutureTask("Viewing Items", resultF)))
        }
      }
    }

    useLayoutEffect({ () =>
      // start scan
      maybeCurrData.foreach { case ((dispatch, actions), state) =>
        inProgress.current = true
        setCurrDir("")
        viewItems(dispatch, actions, state)
      }
      ()
    }, Nil)
    
    maybeCurrData.map { case ((_, _), _) =>
      <(statusPopupComp())(^.wrapped := StatusPopupProps(
        text = s"Scanning the folder\n$currDir",
        title = "View",
        closable = true,
        onClose = { () =>
          // stop scan
          inProgress.current = false
        }
      ))()
    }.orNull
  }
}
