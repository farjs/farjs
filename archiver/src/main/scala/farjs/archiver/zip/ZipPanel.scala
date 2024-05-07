package farjs.archiver.zip

import farjs.archiver._
import farjs.archiver.zip.ZipPanel._
import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack.WithPanelStacks
import farjs.ui.Dispatch
import farjs.ui.popup.{MessageBox, MessageBoxAction, MessageBoxProps}
import farjs.ui.task.{Task, TaskAction}
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed.BlessedScreen
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.Failure

class ZipPanel(zipPath: String,
               rootPath: String,
               entriesByParentF: Future[Map[String, List[ZipEntry]]],
               onClose: () => Unit
              ) extends FunctionComponent[FileListPanelProps] {

  protected def render(compProps: Props): ReactElement = {
    val stacks = WithPanelStacks.usePanelStacks
    val (zipData, setZipData) =
      useState(Option.empty[(Dispatch, FileListActions, FileListState, Seq[FileListItem], Boolean)])
    val (showWarning, setShowWarning) = useState(false)
    val props = compProps.wrapped
    val theme = Theme.useTheme().popup
    
    def onClosePanel(): Unit = {
      val stackData = {
        val stackItem = stacks.activeStack.peekLast[FileListState]
        stackItem.getData
      }
      stackData.foreach { case FileListData(dispatch, actions, state) =>
        dispatch(actions.updateDir(dispatch, state.currDir.path))
      }
      onClose()
    }

    useLayoutEffect({ () =>
      if (props.state.currDir.items.isEmpty) {
        val zipF = entriesByParentF.map { entriesByParent =>
          val totalSize = entriesByParent.foldLeft(0.0) { (total, entry) =>
            total + entry._2.foldLeft(0.0)(_ + _.size)
          }
          props.dispatch(FileListDiskSpaceUpdatedAction(totalSize))
          props.dispatch(FileListDirChangedAction(FileListItem.currDir.name, FileListDir(
            path = rootPath,
            isRoot = false,
            items = js.Array(entriesByParent.getOrElse("", Nil).map(ZipApi.convertToFileListItem): _*)
          )))
        }.andThen {
          case Failure(_) =>
            props.dispatch(FileListDirChangedAction(FileListItem.currDir.name, FileListDir(
              path = rootPath,
              isRoot = false,
              items = js.Array()
            )))
        }

        props.dispatch(TaskAction(Task("Reading zip archive", zipF)))
      }
      ()
    }, Nil)
    
    def onKeypress(screen: BlessedScreen, key: String): Boolean = {
      var processed = true
      key match {
        case "C-pageup" if props.state.currDir.path == rootPath =>
          onClosePanel()
        case "enter" | "C-pagedown" if (
          FileListState.currentItem(props.state).exists(i => i.isDir && i.name == FileListItem.up.name)
            && props.state.currDir.path == rootPath
          ) =>
          onClosePanel()
        case k@(FileListEvent.onFileListCopy | FileListEvent.onFileListMove) =>
          if (props.state.currDir.path != rootPath) setShowWarning(true)
          else {
            val stackData = {
              val stackItem = stacks.activeStack.peek[FileListState]
              stackItem.getData
            }
            stackData.foreach { case FileListData(dispatch, actions, state) =>
              val items =
                if (state.selectedNames.nonEmpty) FileListState.selectedItems(state).toList
                else FileListState.currentItem(state).filter(_ != FileListItem.up).toList

              if (actions.api.isLocal && items.nonEmpty) {
                setZipData(Some((dispatch, actions, state, items, k == FileListEvent.onFileListMove)))
              }
              else processed = false
            }
          }
        case _ =>
          processed = false
      }

      processed
    }

    <.>()(
      <(fileListPanelComp())(^.wrapped := props.copy(onKeypress = onKeypress))(),

      if (showWarning) Some(
        <(messageBoxComp)(^.plain := MessageBoxProps(
          title = "Warning",
          message = "Items can only be added to zip root.",
          actions = js.Array(MessageBoxAction.OK { () =>
            setShowWarning(false)
          }),
          style = theme.regular
        ))()
      ) else None,

      zipData.map { case (dispatch, actions, state, items, move) =>
        <(addToArchController())(^.wrapped := AddToArchControllerProps(
          dispatch = dispatch,
          actions = actions,
          state = state,
          zipName = zipPath,
          items = items,
          action =
            if (move) AddToArchAction.Move
            else AddToArchAction.Copy,
          onComplete = { _ =>
            setZipData(None)

            val updateAction = props.actions.updateDir(props.dispatch, props.state.currDir.path)
            props.dispatch(updateAction)
            if (move) {
              updateAction.task.result.toFuture.foreach { _ =>
                dispatch(actions.deleteItems(dispatch, state.currDir.path, js.Array(items: _*)))
              }
            }
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
  private[zip] var addToArchController: UiComponent[AddToArchControllerProps] = AddToArchController
  private[zip] var messageBoxComp: ReactClass = MessageBox
}
