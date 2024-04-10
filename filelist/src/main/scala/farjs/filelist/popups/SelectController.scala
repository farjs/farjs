package farjs.filelist.popups

import farjs.filelist.FileListActions.FileListParamsChangedAction
import farjs.filelist.api.FileListItem
import farjs.filelist.{FileListServices, FileListUiData}
import scommons.react._

import java.util.regex.Pattern
import scala.scalajs.js

object SelectController extends FunctionComponent[FileListUiData] {

  private[popups] var selectPopupComp: UiComponent[SelectPopupProps] = SelectPopup

  protected def render(compProps: Props): ReactElement = {
    val services = FileListServices.useServices
    val props = compProps.wrapped

    (props.data, props.showSelectPopup) match {
      case (Some(data), Some(showSelectPopup)) =>
        <(selectPopupComp())(^.wrapped := SelectPopupProps(
          showSelect = showSelectPopup,
          onAction = { pattern =>
            services.selectPatternsHistory.save(pattern)

            val regexes = pattern.split(';')
              .map(mask => Pattern.compile(fileMaskToRegex(mask)))
            val matchedNames = data.state.currDir.items
              .filter(i => i != FileListItem.up && regexes.exists(_.matcher(i.name).matches()))
              .map(_.name)

            val currSelected = data.state.selectedNames.toSet
            val updatedSelection =
              if (showSelectPopup) {
                currSelected ++ matchedNames
              }
              else currSelected -- matchedNames

            if (updatedSelection != currSelected) {
              data.dispatch(FileListParamsChangedAction(
                offset = data.state.offset,
                index = data.state.index,
                selectedNames = js.Set[String](updatedSelection.toSeq: _*)
              ))
            }
            props.onClose()
          },
          onCancel = props.onClose
        ))()
      case _ => null
    }
  }

  // consider supporting full glob pattern:
  //  https://stackoverflow.com/questions/1247772/is-there-an-equivalent-of-java-util-regex-for-glob-type-patterns/17369948#17369948
  //
  private[popups] def fileMaskToRegex(mask: String): String = {
    "^" +
      escapeSpecials(mask)
        .replace("$", "\\$")
        .replace("\\*", ".*?")
        .replace("\\?", ".") +
      "$"
  }
  
  private val escapeRegex = """[.*+\-?^{}()|\[\]\\]""".r
  
  // got from:
  //   https://stackoverflow.com/questions/3561493/is-there-a-regexp-escape-function-in-javascript/63838890#63838890
  //
  private def escapeSpecials(regex: String): String = {
    escapeRegex.replaceAllIn(regex, { group =>
      s"\\\\${group.matched}"
    })
  }
}
