package farclone.ui.filelist.popups

import farclone.ui.filelist.FileListsStateDef
import farclone.ui.filelist.popups.FileListPopupsActions._
import farclone.ui.popup._
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.nodejs._
import scommons.react._

import scala.scalajs.js

case class FileListPopupsProps(dispatch: Dispatch,
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
          message = "Do you really want to exit FARc?",
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
              //props.dispatch(FileListPopupDeleteAction(show = false))
              //TODO: add api call
            },
            MessageBoxAction.NO { () =>
              props.dispatch(FileListPopupDeleteAction(show = false))
            }
          )
        ))()
      ) else None
    )
  }
}
