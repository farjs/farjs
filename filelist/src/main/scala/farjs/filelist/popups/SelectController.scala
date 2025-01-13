package farjs.filelist.popups

import farjs.filelist.FileListActions.FileListParamsChangedAction
import farjs.filelist.FileListUiData
import farjs.filelist.api.FileListItem
import farjs.filelist.history.{History, HistoryKind, HistoryProvider}
import scommons.react._

import java.util.regex.Pattern
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object SelectController extends FunctionComponent[FileListUiData] {

  val selectPatternsHistoryKind: HistoryKind = HistoryKind("farjs.selectPatterns", 50)

  private[popups] var selectPopupComp: UiComponent[SelectPopupProps] = SelectPopup

  protected def render(compProps: Props): ReactElement = {
    val historyProvider = HistoryProvider.useHistoryProvider()
    val props = compProps.plain

    (props.data.toOption, props.showSelectPopup.toOption) match {
      case (Some(data), Some(showSelectPopup)) =>
        <(selectPopupComp())(^.wrapped := SelectPopupProps(
          showSelect = showSelectPopup,
          onAction = { pattern =>
            for {
              selectPatternsHistory <- historyProvider.get(selectPatternsHistoryKind).toFuture
              _ <- selectPatternsHistory.save(History(pattern, js.undefined)).toFuture
            } yield ()

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
