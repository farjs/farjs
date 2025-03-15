package farjs.fs.popups

import scommons.react._

object FoldersHistoryController extends FunctionComponent[FoldersHistoryControllerProps] {

  private[popups] var foldersHistoryPopup: UiComponent[FoldersHistoryPopupProps] = FoldersHistoryPopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain

    if (props.showPopup) {
      <(foldersHistoryPopup())(^.wrapped := FoldersHistoryPopupProps(
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
