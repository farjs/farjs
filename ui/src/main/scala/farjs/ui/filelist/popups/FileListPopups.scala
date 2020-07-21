package farjs.ui.filelist.popups

import farjs.ui.filelist.popups.FileListPopupsActions._
import farjs.ui.filelist.{FileListActions, FileListsStateDef}
import farjs.ui.popup._
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.nodejs._
import scommons.react._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

case class FileListPopupsProps(dispatch: Dispatch,
                               actions: FileListActions,
                               data: FileListsStateDef)

object FileListPopups extends FunctionComponent[FileListPopupsProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val popups = props.data.popups

    <.>()(
      if (popups.showHelpPopup) Some(
        <(MessageBox())(^.wrapped := MessageBoxProps(
          title = "Help",
          message = "//TODO: show help/about info",
          actions = List(MessageBoxAction.OK { () =>
            props.dispatch(FileListPopupHelpAction(show = false))
          })
        ))()
      ) else None,
      
      if (popups.showExitPopup) Some(
        <(MessageBox())(^.wrapped := MessageBoxProps(
          title = "Exit",
          message = "Do you really want to exit FAR.js?",
          actions = List(
            MessageBoxAction.YES { () =>
              props.dispatch(FileListPopupExitAction(show = false))
              process.stdin.emit("keypress", js.undefined, js.Dynamic.literal(
                name = "c",
                ctrl = true,
                meta = false,
                shift = false
              ))
            },
            MessageBoxAction.NO { () =>
              props.dispatch(FileListPopupExitAction(show = false))
            }
          )
        ))()
      ) else None,
      
      if (popups.showDeletePopup) Some(
        <(MessageBox())(^.wrapped := MessageBoxProps(
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
          style = Popup.Styles.error
        ))()
      ) else None,
      
      if (popups.showMkFolderPopup) Some(
        <(MakeFolderPopup())(^.wrapped := MakeFolderPopupProps(
          folderName = "",
          multiple = false,
          onOk = { (_, _) =>
            //TODO: add api call
            //props.dispatch(FileListPopupMkFolderAction(show = false))
          },
          onCancel = { () =>
            props.dispatch(FileListPopupMkFolderAction(show = false))
          }
        ))()
      ) else None
    )
  }
}
