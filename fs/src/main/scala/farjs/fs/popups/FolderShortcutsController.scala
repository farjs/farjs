package farjs.fs.popups

import scommons.react._

object FolderShortcutsController extends FunctionComponent[FolderShortcutsControllerProps] {

  private[popups] var folderShortcutsPopup: UiComponent[FolderShortcutsPopupProps] = FolderShortcutsPopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain

    if (props.showPopup) {
      <(folderShortcutsPopup())(^.wrapped := FolderShortcutsPopupProps(
        onChangeDir = { dir =>
          props.onClose()
          props.onChangeDir(dir)
        },
        onClose = props.onClose
      ))()
    }
    else null
  }
}
