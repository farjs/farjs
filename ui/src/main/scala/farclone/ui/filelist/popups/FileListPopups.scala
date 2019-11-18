package farclone.ui.filelist.popups

import farclone.ui.filelist.popups.FileListPopupsActions._
import farclone.ui.popup._
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.nodejs._
import scommons.react._

import scala.scalajs.js

case class FileListPopupsProps(dispatch: Dispatch,
                               data: FileListPopupsState)

object FileListPopups extends FunctionComponent[FileListPopupsProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    <.>()(
      if (props.data.showHelpPopup) Some(
        <(MessageBox())(^.wrapped := MessageBoxProps(
          title = "Help",
          message = "//TODO: show help/about info",
          actions = List(MessageBoxAction.OK { () =>
            props.dispatch(FileListHelpAction(show = false))
          })
        ))()
      ) else None,
      
      if (props.data.showExitPopup) Some(
        <(MessageBox())(^.wrapped := MessageBoxProps(
          title = "Exit",
          message = "Do you really want to exit FARc?",
          actions = List(
            MessageBoxAction.YES { () =>
              props.dispatch(FileListExitAction(show = false))
              process.stdin.emit("keypress", js.undefined, js.Dynamic.literal(
                name = "c",
                ctrl = true,
                meta = false,
                shift = false
              ))
            },
            MessageBoxAction.NO { () =>
              props.dispatch(FileListExitAction(show = false))
            }
          )
        ))()
      ) else None
    )
  }
}
