package farjs.filelist.popups

import farjs.filelist.popups.FileListPopupsActions._
import scommons.react._

object MenuController extends FunctionComponent[FileListPopupsProps] {

  private[popups] var menuBarComp: UiComponent[MenuBarProps] = MenuBar

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val popups = props.popups

    if (popups.showMenuPopup) {
      <(menuBarComp())(^.wrapped := MenuBarProps(
        onClose = { () =>
          props.dispatch(FileListPopupMenuAction(show = false))
        }
      ))()
    }
    else null
  }
}
