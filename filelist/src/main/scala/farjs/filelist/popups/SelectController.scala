package farjs.filelist.popups

import farjs.filelist.FileListActions.FileListParamsChangedAction
import farjs.filelist.popups.FileListPopupsActions._
import scommons.react._
import scommons.react.hooks._

import java.util.regex.Pattern

object SelectController extends FunctionComponent[PopupControllerProps] {

  private[popups] var selectPopupComp: UiComponent[SelectPopupProps] = SelectPopup

  protected def render(compProps: Props): ReactElement = {
    val (selectPattern, setSelectPattern) = useState("")
    val props = compProps.wrapped
    val popups = props.popups

    props.data match {
      case Some(data) if popups.showSelectPopup != SelectHidden =>
        <(selectPopupComp())(^.wrapped := SelectPopupProps(
          pattern = selectPattern,
          action = popups.showSelectPopup,
          onAction = { pattern =>
            setSelectPattern(pattern)

            val regexes = pattern.split(';')
              .map(mask => Pattern.compile(fileMaskToRegex(mask)))
            val matchedNames = data.state.currDir.items
              .map(_.name)
              .filter(n => regexes.exists(_.matcher(n).matches()))
            val updatedSelection =
              if (popups.showSelectPopup == ShowSelect) {
                data.state.selectedNames ++ matchedNames
              }
              else data.state.selectedNames -- matchedNames

            if (updatedSelection != data.state.selectedNames) {
              data.dispatch(FileListParamsChangedAction(
                offset = data.state.offset,
                index = data.state.index,
                selectedNames = updatedSelection
              ))
            }
            data.dispatch(FileListPopupSelectAction(SelectHidden))
          },
          onCancel = { () =>
            data.dispatch(FileListPopupSelectAction(SelectHidden))
          }
        ))()
      case _ => null
    }
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
