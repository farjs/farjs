package farjs.filelist

import farjs.filelist.FileListActions.FileListParamsChangedAction
import farjs.filelist.api.FileListItem
import farjs.filelist.popups.FileListPopupsActions._
import scommons.nodejs.path
import scommons.react._
import scommons.react.hooks._
import scommons.react.blessed.BlessedScreen
import scommons.react.redux.Dispatch

case class FileListPanelProps(dispatch: Dispatch,
                              actions: FileListActions,
                              state: FileListState,
                              onKeypress: (BlessedScreen, String) => Boolean = (_, _) => false)

object FileListPanel extends FunctionComponent[FileListPanelProps] {

  private[filelist] var fileListPanelView: UiComponent[FileListPanelViewProps] = FileListPanelView
  private[filelist] var fileListQuickSearch: UiComponent[FileListQuickSearchProps] = FileListQuickSearch

  protected def render(compProps: Props): ReactElement = {
    val (maybeQuickSearch, setMaybeQuickSearch) = useState(Option.empty[String])
    val props = compProps.wrapped

    def quickSearch(text: String): Unit = {
      val index = props.state.currDir.items.indexWhere(_.name.startsWith(text))
      if (index >= 0) {
        props.dispatch(FileListParamsChangedAction(
          offset = 0,
          index = index,
          selectedNames = props.state.selectedNames
        ))
        
        setMaybeQuickSearch(Some(text))
      }
    }

    def onKeypress(screen: BlessedScreen, key: String): Unit = {
      if (!props.onKeypress(screen, key)) {
        val currItem = props.state.currentItem.filter(_ != FileListItem.up)
        key match {
          case "f1" => props.dispatch(FileListPopupHelpAction(show = true))
          case "f3" =>
            if (props.state.selectedNames.nonEmpty || currItem.exists(_.isDir)) {
              props.dispatch(FileListPopupViewItemsAction(show = true))
            }
          case "S-f5" =>
            if (currItem.nonEmpty) {
              props.dispatch(FileListPopupCopyMoveAction(ShowCopyInplace))
            }
          case "S-f6" =>
            if (currItem.nonEmpty) {
              props.dispatch(FileListPopupCopyMoveAction(ShowMoveInplace))
            }
          case "f7" => props.dispatch(FileListPopupMkFolderAction(show = true))
          case "f8" | "delete" =>
            if (props.state.selectedNames.nonEmpty || currItem.isDefined) {
              props.dispatch(FileListPopupDeleteAction(show = true))
            }
          case "C-c" =>
            props.state.currentItem.foreach { item =>
              val text =
                if (item.name == FileListItem.up.name) props.state.currDir.path
                else path.join(props.state.currDir.path, item.name)
              screen.copyToClipboard(text)
            }
          case "C-r" =>
            props.dispatch(props.actions.updateDir(props.dispatch, props.state.currDir.path))
          case k@("enter" | "C-pageup" | "C-pagedown") =>
            val targetDir = k match {
              case "C-pageup" => Some(FileListItem.up)
              case _ => props.state.currentItem.filter(_.isDir)
            }
            targetDir.foreach { dir =>
              props.dispatch(props.actions.changeDir(
                dispatch = props.dispatch,
                parent = Some(props.state.currDir.path),
                dir = dir.name
              ))
            }
          case "C-s" => setMaybeQuickSearch(Some(""))
          case _ =>
        }
      }

      maybeQuickSearch.foreach { text =>
        if (key.length == 1) quickSearch(s"$text$key")
        else if (key.startsWith("S-") && key.length == 3) quickSearch(s"""$text${key.drop(2).toUpperCase}""")
        else if (key == "space") quickSearch(s"$text ")
        else if (key == "backspace") setMaybeQuickSearch(Some(text.take(text.length - 1)))
        else if (key != "C-s" && key.length > 1) setMaybeQuickSearch(None)
      }
    }
    
    useLayoutEffect({ () =>
      if (!props.state.isActive) {
        setMaybeQuickSearch(None)
      }
    }, List(props.state.isActive))
  
    <.>()(
      <(fileListPanelView())(^.wrapped := FileListPanelViewProps(
        dispatch = props.dispatch,
        actions = props.actions,
        state = props.state,
        onKeypress = onKeypress
      ))(),

      maybeQuickSearch.map { text =>
        <(fileListQuickSearch())(^.wrapped := FileListQuickSearchProps(
          text = text,
          onClose = { () =>
            setMaybeQuickSearch(None)
          }
        ))()
      }
    )
  }
}
