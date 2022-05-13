package farjs.app.filelist.zip

import farjs.app.filelist.zip.ZipPanel._
import farjs.filelist.FileListActions._
import farjs.filelist._
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack.WithPanelStacks
import farjs.ui.popup.{MessageBox, MessageBoxAction, MessageBoxProps}
import farjs.ui.theme.Theme
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
    val (zipData, setZipData) =
      useState(Option.empty[(Dispatch, FileListActions, FileListState, Seq[FileListItem], Boolean)])
    val (showWarning, setShowWarning) = useState(false)
    val props = compProps.wrapped
    val theme = Theme.current.popup

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
        case k@(FileListEvent.onFileListCopy | FileListEvent.onFileListMove) =>
          if (props.state.currDir.path != rootPath) setShowWarning(true)
          else {
            val stackData = {
              val stackItem = stacks.activeStack.peek[FileListState]
              stackItem.getActions.zip(stackItem.state)
            }
            stackData.foreach { case ((dispatch, actions), state) =>
              val items = {
                if (state.selectedNames.nonEmpty) state.selectedItems
                else {
                  val currItem = state.currentItem.filter(_ != FileListItem.up)
                  currItem.toList
                }
              }
              if (actions.isLocalFS && items.nonEmpty) {
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
        <(messageBoxComp())(^.wrapped := MessageBoxProps(
          title = "Warning",
          message = "Items can only be added to zip root.",
          actions = List(MessageBoxAction.OK { () =>
            setShowWarning(false)
          }),
          style = theme.regular
        ))()
      ) else None,

      zipData.map { case (dispatch, actions, state, items, move) =>
        <(addToZipController())(^.wrapped := AddToZipControllerProps(
          dispatch = dispatch,
          state = state,
          zipName = zipPath,
          items = items.map(_.name).toSet,
          action =
            if (move) AddToZipAction.Move
            else AddToZipAction.Copy,
          onComplete = { _ =>
            setZipData(None)

            val updateAction = props.actions.updateDir(props.dispatch, props.state.currDir.path)
            props.dispatch(updateAction)
            if (move) {
              updateAction.task.future.foreach { _ =>
                dispatch(actions.deleteAction(dispatch, state.currDir.path, items))
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
  private[zip] var addToZipController: UiComponent[AddToZipControllerProps] = AddToZipController
  private[zip] var messageBoxComp: UiComponent[MessageBoxProps] = MessageBox
}
