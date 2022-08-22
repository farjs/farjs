package farjs.filelist.popups

import farjs.filelist.popups.FileListPopupsActions._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.nodejs._
import scommons.react._

import scala.scalajs.js

object ExitController extends FunctionComponent[FileListPopupsProps] {

  private[popups] var messageBoxComp: UiComponent[MessageBoxProps] = MessageBox

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val popups = props.popups
    val theme = Theme.current.popup

    if (popups.showExitPopup) {
      <(messageBoxComp())(^.plain := MessageBoxProps(
        title = "Exit",
        message = "Do you really want to exit FAR.js?",
        actions = js.Array(
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
    }
    else null
  }
}
