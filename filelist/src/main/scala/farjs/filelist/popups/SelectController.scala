package farjs.filelist.popups

import farjs.filelist.FileListActions.FileListParamsChangedAction
import farjs.filelist.popups.FileListPopupsActions._
import farjs.filelist.{FileListActions, FileListState}
import scommons.react._
import scommons.react.hooks._
import scommons.react.redux.Dispatch

import java.util.regex.Pattern

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

          val regexes = pattern.split(';')
            .map(mask => Pattern.compile(fileMaskToRegex(mask)))
          val matchedNames = props.state.currDir.items
            .map(_.name)
            .filter(n => regexes.exists(_.matcher(n).matches()))
          val updatedSelection =
            if (popups.showSelectPopup == ShowSelect) {
              props.state.selectedNames ++ matchedNames
            }
            else props.state.selectedNames -- matchedNames

          if (updatedSelection != props.state.selectedNames) {
            props.dispatch(FileListParamsChangedAction(
              offset = props.state.offset,
              index = props.state.index,
              selectedNames = updatedSelection
            ))
          }
          props.dispatch(FileListPopupSelectAction(SelectHidden))
        },
        onCancel = { () =>
          props.dispatch(FileListPopupSelectAction(SelectHidden))
        }
      ))()
    }
    else null
  }

  // consider supporting full glob pattern:
  //  https://stackoverflow.com/questions/1247772/is-there-an-equivalent-of-java-util-regex-for-glob-type-patterns/17369948#17369948
  //
  private[popups] def fileMaskToRegex(mask: String): String = {
    "^" +
      Pattern.quote(mask)
        .replace("\\*", ".*?")
        .replace("\\?", ".") +
      "$"
  }
}
