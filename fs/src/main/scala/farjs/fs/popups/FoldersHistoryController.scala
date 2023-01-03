package farjs.fs.popups

import FSPopupsActions.FoldersHistoryPopupAction
import scommons.react._
import scommons.react.redux.Dispatch

case class FoldersHistoryControllerProps(dispatch: Dispatch,
                                         showPopup: Boolean,
                                         onChangeDir: String => Unit)

object FoldersHistoryController extends FunctionComponent[FoldersHistoryControllerProps] {

  private[popups] var foldersHistoryPopup: UiComponent[FoldersHistoryPopupProps] = FoldersHistoryPopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    if (props.showPopup) {
      <(foldersHistoryPopup())(^.wrapped := FoldersHistoryPopupProps(
        onChangeDir = { dir =>
          props.dispatch(FoldersHistoryPopupAction(show = false))
          props.onChangeDir(dir)
        },
        onClose = { () =>
          props.dispatch(FoldersHistoryPopupAction(show = false))
        }
      ))()
    }
    else null
  }
}
