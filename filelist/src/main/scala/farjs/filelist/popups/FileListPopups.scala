package farjs.filelist.popups

import farjs.filelist.copy.CopyItems
import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.{FileListActions, FileListsStateDef}
import farjs.ui.popup._
import farjs.ui.theme.Theme
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.nodejs._
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

case class FileListPopupsProps(dispatch: Dispatch,
                               actions: FileListActions,
                               data: FileListsStateDef)

object FileListPopups extends FunctionComponent[FileListPopupsProps] {

  private[popups] var messageBoxComp: UiComponent[MessageBoxProps] = MessageBox
  private[popups] var makeFolderPopupComp: UiComponent[MakeFolderPopupProps] = MakeFolderPopup
  private[popups] var viewItemsPopupComp: UiComponent[FileListPopupsProps] = ViewItemsPopup
  private[popups] var copyItemsComp: UiComponent[FileListPopupsProps] = CopyItems

  protected def render(compProps: Props): ReactElement = {
    val (folderName, setFolderName) = useState("")
    val (multiple, setMultiple) = useState(false)
    val props = compProps.wrapped
    val popups = props.data.popups
    val theme = Theme.current.popup

    <.>()(
      if (popups.showHelpPopup) Some(
        <(messageBoxComp())(^.wrapped := MessageBoxProps(
          title = "Help",
          message = "//TODO: show help/about info",
          actions = List(MessageBoxAction.OK { () =>
            props.dispatch(FileListPopupHelpAction(show = false))
          }),
          style = theme.regular
        ))()
      ) else None,
      
      if (popups.showExitPopup) Some(
        <(messageBoxComp())(^.wrapped := MessageBoxProps(
          title = "Exit",
          message = "Do you really want to exit FAR.js?",
          actions = List(
            MessageBoxAction.YES { () =>
              props.dispatch(FileListPopupExitAction(show = false))
              process.stdin.emit("keypress", js.undefined, js.Dynamic.literal(
                name = "e",
                ctrl = true,
                meta = false,
                shift = false
              ))
            },
            MessageBoxAction.NO { () =>
              props.dispatch(FileListPopupExitAction(show = false))
            }
          ),
          style = theme.regular
        ))()
      ) else None,
      
      if (popups.showDeletePopup) Some(
        <(messageBoxComp())(^.wrapped := MessageBoxProps(
          title = "Delete",
          message = "Do you really want to delete selected item(s)?",
          actions = List(
            MessageBoxAction.YES { () =>
              val state = props.data.activeList
              val items =
                if (state.selectedItems.nonEmpty) state.selectedItems
                else state.currentItem.toList
              
              val deleteAction = props.actions.deleteItems(
                dispatch = props.dispatch,
                isRight = state.isRight,
                dir = state.currDir.path,
                items = items
              )
              deleteAction.task.future.foreach { _ =>
                props.dispatch(FileListPopupDeleteAction(show = false))
                props.dispatch(props.actions.updateDir(props.dispatch, state.isRight, state.currDir.path))
              }
              props.dispatch(deleteAction)
            },
            MessageBoxAction.NO { () =>
              props.dispatch(FileListPopupDeleteAction(show = false))
            }
          ),
          style = theme.error
        ))()
      ) else None,
      
      if (popups.showMkFolderPopup) Some(
        <(makeFolderPopupComp())(^.wrapped := MakeFolderPopupProps(
          folderName = folderName,
          multiple = multiple,
          onOk = { (dir, multiple) =>
            val state = props.data.activeList
            val action = props.actions.createDir(
              dispatch = props.dispatch,
              isRight = state.isRight,
              parent = state.currDir.path,
              dir = dir,
              multiple = multiple
            )
            action.task.future.foreach { _ =>
              setFolderName(dir)
              setMultiple(multiple)
              props.dispatch(FileListPopupMkFolderAction(show = false))
            }
            props.dispatch(action)
          },
          onCancel = { () =>
            props.dispatch(FileListPopupMkFolderAction(show = false))
          }
        ))()
      ) else None,

      <(viewItemsPopupComp())(^.wrapped := props)(),
      <(copyItemsComp())(^.wrapped := props)()
    )
  }
}
