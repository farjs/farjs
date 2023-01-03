package farjs.fs.popups

import FSPopupsActions.FolderShortcutsPopupAction
import scommons.react._
import scommons.react.redux.Dispatch

case class FolderShortcutsControllerProps(dispatch: Dispatch,
                                          showPopup: Boolean,
                                          onChangeDir: String => Unit)

object FolderShortcutsController extends FunctionComponent[FolderShortcutsControllerProps] {

  private[popups] var folderShortcutsPopup: UiComponent[FolderShortcutsPopupProps] = FolderShortcutsPopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    if (props.showPopup) {
      <(folderShortcutsPopup())(^.wrapped := FolderShortcutsPopupProps(
        onChangeDir = { dir =>
          props.dispatch(FolderShortcutsPopupAction(show = false))
          props.onChangeDir(dir)
        },
        onClose = { () =>
          props.dispatch(FolderShortcutsPopupAction(show = false))
        }
      ))()
    }
    else null
  }
}
