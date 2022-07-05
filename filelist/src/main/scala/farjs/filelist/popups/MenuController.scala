package farjs.filelist.popups

import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.stack.WithPanelStacks
import farjs.ui.popup.PopupOverlay
import scommons.react._
import scommons.react.blessed._

object MenuController extends FunctionComponent[FileListPopupsProps] {

  private[popups] var menuBarComp: UiComponent[MenuBarProps] = MenuBar

  protected def render(compProps: Props): ReactElement = {
    val stacks = WithPanelStacks.usePanelStacks
    val props = compProps.wrapped
    val popups = props.popups

    if (popups.showMenuPopup) {
      <(menuBarComp())(^.wrapped := MenuBarProps(
        leftInput = stacks.leftInput,
        rightInput = stacks.rightInput,
        onClose = { () =>
          props.dispatch(FileListPopupMenuAction(show = false))
        }
      ))()
    }
    else {
      <.box(
        ^.rbHeight := 1,
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := PopupOverlay.style,
        ^.rbOnClick := { _ =>
          props.dispatch(FileListPopupMenuAction(show = true))
        }
      )()
    }
  }
}
