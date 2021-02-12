package farjs.filelist.popups

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
                name = "q",
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
              val state =
                if (props.data.left.isActive) props.data.left
                else props.data.right

              val items =
                if (state.selectedNames.nonEmpty) {
                  state.currDir.items.filter(i => state.selectedNames.contains(i.name))
                }
                else state.currentItem.toList
              
              val action = props.actions.deleteItems(
                dispatch = props.dispatch,
                isRight = state.isRight,
                dir = state.currDir.path,
                items = items
              )
              action.task.future.foreach { _ =>
                props.dispatch(FileListPopupDeleteAction(show = false))
              }
              props.dispatch(action)
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
            val state =
              if (props.data.left.isActive) props.data.left
              else props.data.right

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
      ) else None
    )
  }
}
