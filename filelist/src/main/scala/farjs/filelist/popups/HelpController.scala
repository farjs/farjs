package farjs.filelist.popups

import farjs.filelist.popups.FileListPopupsActions._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._

import scala.scalajs.js

object HelpController extends FunctionComponent[FileListPopupsProps] {

  private[popups] var messageBoxComp: UiComponent[MessageBoxProps] = MessageBox

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val popups = props.popups
    val theme = Theme.current.popup

    if (popups.showHelpPopup) {
      <(messageBoxComp())(^.plain := MessageBoxProps(
        title = "Help",
        message = "//TODO: show help/about info",
        actions = js.Array(MessageBoxAction.OK { () =>
          props.dispatch(FileListPopupHelpAction(show = false))
        }),
        style = theme.regular
      ))()
    }
    else null
  }
}
