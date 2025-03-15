package farjs.fs.popups

import scommons.react._

import scala.scalajs.js

case class FolderShortcutsControllerProps(showPopup: Boolean,
                                          onChangeDir: js.Function1[String, Unit],
                                          onClose: js.Function0[Unit])

object FolderShortcutsController extends FunctionComponent[FolderShortcutsControllerProps] {

  private[popups] var folderShortcutsPopup: UiComponent[FolderShortcutsPopupProps] = FolderShortcutsPopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

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
