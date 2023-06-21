package farjs.filelist.popups

import farjs.filelist.FileListUiData
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._

import scala.scalajs.js

object HelpController extends FunctionComponent[FileListUiData] {

  private[popups] var messageBoxComp: UiComponent[MessageBoxProps] = MessageBox

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val theme = Theme.useTheme.popup

    if (props.showHelpPopup) {
      <(messageBoxComp())(^.plain := MessageBoxProps(
        title = "Help",
        message = "//TODO: show help/about info",
        actions = js.Array(MessageBoxAction.OK { () =>
          props.onClose()
        }),
        style = theme.regular
      ))()
    }
    else null
  }
}
