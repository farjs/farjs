package farjs.app.filelist.zip

import farjs.app.filelist.zip.ZipPanel._
import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack.WithPanelStacks
import scommons.react._
import scommons.react.blessed.BlessedScreen
import scommons.react.hooks._
import scommons.react.redux.Dispatch
import scommons.react.redux.task.FutureTask

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure

class ZipPanel(zipPath: String,
               rootPath: String,
               entriesByParentF: Future[Map[String, List[ZipEntry]]],
               onClose: () => Unit
              ) extends FunctionComponent[FileListPanelProps] {

  protected def render(compProps: Props): ReactElement = {
    val stacks = WithPanelStacks.usePanelStacks
    val (zipData, setZipData) = useState(Option.empty[(Dispatch, FileListState, Set[String])])
    val props = compProps.wrapped

    useLayoutEffect({ () =>
      if (props.state.currDir.items.isEmpty) {
        val zipF = entriesByParentF.map { entriesByParent =>
          val totalSize = entriesByParent.foldLeft(0.0) { (total, entry) =>
            total + entry._2.foldLeft(0.0)(_ + _.size)
          }
          props.dispatch(FileListDiskSpaceUpdatedAction(totalSize))
          props.dispatch(FileListDirChangedAction(FileListDir.curr, FileListDir(
            path = rootPath,
            isRoot = false,
            items = entriesByParent.getOrElse("", Nil).map(ZipApi.convertToFileListItem)
          )))
        }.andThen {
          case Failure(_) =>
            props.dispatch(FileListDirChangedAction(FileListDir.curr, FileListDir(
              path = rootPath,
              isRoot = false,
              items = Nil
            )))
        }

        props.dispatch(FileListTaskAction(FutureTask("Reading zip archive", zipF)))
      }
      ()
    }, Nil)
    
    def onKeypress(screen: BlessedScreen, key: String): Boolean = {
      var processed = true
      key match {
        case "C-pageup" if props.state.currDir.path == rootPath =>
          onClose()
        case "enter" | "C-pagedown" if (
          props.state.currentItem.exists(i => i.isDir && i.name == FileListItem.up.name)
            && props.state.currDir.path == rootPath
          ) =>
          onClose()
        case FileListEvent.onFileListCopy =>
          val stackData = {
            val stackItem = stacks.activeStack.peek[FileListState]
            stackItem.getActions.zip(stackItem.state)
          }
          stackData.foreach { case ((dispatch, actions), state) =>
            val items = {
              if (state.selectedNames.nonEmpty) state.selectedNames
              else {
                val currItem = state.currentItem.filter(_ != FileListItem.up)
                currItem.map(_.name).toSet
              }
            }
            if (actions.isLocalFS && items.nonEmpty) {
              setZipData(Some((dispatch, state, items)))
            }
            else processed = false
          }
        case _ =>
          processed = false
      }

      processed
    }

    <.>()(
      <(fileListPanelComp())(^.wrapped := props.copy(onKeypress = onKeypress))(),

      zipData.map { case (dispatch, state, items) =>
        <(addToZipController())(^.wrapped := AddToZipControllerProps(
          dispatch = dispatch,
          state = state,
          zipName = zipPath,
          items = items,
          onComplete = { _ =>
            setZipData(None)
            props.dispatch(props.actions.updateDir(props.dispatch, props.state.currDir.path))
          },
          onCancel = { () =>
            setZipData(None)
          }
        ))()
      }
    )
  }
}

object ZipPanel {

  private[zip] var fileListPanelComp: UiComponent[FileListPanelProps] = FileListPanel
  private[zip] var addToZipController: UiComponent[AddToZipControllerProps] = AddToZipController
}
