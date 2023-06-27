package farjs.filelist.popups

import farjs.filelist.FileListUiData
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.nodejs._
import scommons.react._

import scala.scalajs.js

object ExitController extends FunctionComponent[FileListUiData] {

  private[popups] var messageBoxComp: UiComponent[MessageBoxProps] = MessageBox

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val theme = Theme.useTheme().popup

    if (props.showExitPopup) {
      <(messageBoxComp())(^.plain := MessageBoxProps(
        title = "Exit",
        message = "Do you really want to exit FAR.js?",
        actions = js.Array(
          MessageBoxAction.YES { () =>
            props.onClose()
            process.stdin.emit("keypress", js.undefined, js.Dynamic.literal(
              name = "e",
              ctrl = true,
              meta = false,
              shift = false
            ))
          },
          MessageBoxAction.NO { () =>
            props.onClose()
          }
        ),
        style = theme.regular
      ))()
    }
    else null
  }
}
