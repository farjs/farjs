package farjs.fs.popups

import scommons.react._

import scala.scalajs.js

case class FoldersHistoryControllerProps(showPopup: Boolean,
                                         onChangeDir: String => Unit,
                                         onClose: js.Function0[Unit])

object FoldersHistoryController extends FunctionComponent[FoldersHistoryControllerProps] {

  private[popups] var foldersHistoryPopup: UiComponent[FoldersHistoryPopupProps] = FoldersHistoryPopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

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
