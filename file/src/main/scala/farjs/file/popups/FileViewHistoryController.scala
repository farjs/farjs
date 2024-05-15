package farjs.file.popups

import farjs.file.FileEvent
import farjs.filelist.stack.{WithPanelStacks, PanelStacks}
import scommons.react._

import scala.scalajs.js

case class FileViewHistoryControllerProps(showPopup: Boolean,
                                          onClose: js.Function0[Unit])

object FileViewHistoryController extends FunctionComponent[FileViewHistoryControllerProps] {

  private[popups] var fileViewHistoryPopup: UiComponent[FileViewHistoryPopupProps] =
    FileViewHistoryPopup

  protected def render(compProps: Props): ReactElement = {
    val stacks = WithPanelStacks.usePanelStacks
    val props = compProps.wrapped

    if (props.showPopup) {
      <(fileViewHistoryPopup())(^.wrapped := FileViewHistoryPopupProps(
        onAction = { history =>
          props.onClose()

          PanelStacks.active(stacks).input.emit("keypress", js.undefined, js.Dynamic.literal(
            name = "",
            full = FileEvent.onFileView,
            data = history.asInstanceOf[js.Dynamic]
          ))
        },
        onClose = props.onClose
      ))()
    }
    else null
  }
}
