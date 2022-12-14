package farjs.app.filelist.fs.popups

import farjs.app.filelist.fs.popups.FSPopupsActions.FolderShortcutsPopupAction
import scommons.react._

object FolderShortcutsController extends FunctionComponent[FSPopupsProps] {

  private[popups] var folderShortcutsPopup: UiComponent[FolderShortcutsPopupProps] = FolderShortcutsPopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    if (props.popups.showFolderShortcutsPopup) {
      <(folderShortcutsPopup())(^.wrapped := FolderShortcutsPopupProps(
        onChangeDir = { _ =>
          props.dispatch(FolderShortcutsPopupAction(show = false))
          
          //TODO: change dir
        },
        onClose = { () =>
          props.dispatch(FolderShortcutsPopupAction(show = false))
        }
      ))()
    }
    else null
  }
}
