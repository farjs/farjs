package farjs.filelist.popups

import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.{FileListActions, FileListState}
import scommons.react._
import scommons.react.hooks._
import scommons.react.redux.Dispatch

case class SelectControllerProps(dispatch: Dispatch,
                                 actions: FileListActions,
                                 state: FileListState,
                                 popups: FileListPopupsState)

object SelectController extends FunctionComponent[SelectControllerProps] {

  private[popups] var selectPopupComp: UiComponent[SelectPopupProps] = SelectPopup

  protected def render(compProps: Props): ReactElement = {
    val (selectPattern, setSelectPattern) = useState("")
    val props = compProps.wrapped
    val popups = props.popups

    if (popups.showSelectPopup != SelectHidden) {
      <(selectPopupComp())(^.wrapped := SelectPopupProps(
        pattern = selectPattern,
        action = popups.showSelectPopup,
        onAction = { pattern =>
          setSelectPattern(pattern)
        },
        onCancel = { () =>
          props.dispatch(FileListPopupSelectAction(SelectHidden))
        }
      ))()
    }
    else null
  }
}
