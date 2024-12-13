package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.api.FileListItem
import farjs.filelist.sort._
import farjs.filelist.stack.WithStack
import farjs.ui.Dispatch
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed.BlessedScreen
import scommons.react.hooks._

import scala.scalajs.js

case class FileListPanelProps(dispatch: Dispatch,
                              actions: FileListActions,
                              state: FileListState,
                              onKeypress: (BlessedScreen, String) => Boolean = (_, _) => false)

object FileListPanel extends FunctionComponent[FileListPanelProps] {

  private[filelist] var fileListPanelView: UiComponent[FileListPanelViewProps] = FileListPanelView
  private[filelist] var fileListQuickSearch: UiComponent[FileListQuickSearchProps] = FileListQuickSearch
  private[filelist] var sortModesPopup: ReactClass = SortModesPopup

  protected def render(compProps: Props): ReactElement = {
    val stackProps = WithStack.useStack()
    val (maybeQuickSearch, setMaybeQuickSearch) = useState(Option.empty[String])
    val (showSortModes, setShowSortModes) = useState(false)
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
        key match {
          case "C-f3" => props.dispatch(FileListSortAction(SortMode.Name))
          case "C-f4" => props.dispatch(FileListSortAction(SortMode.Extension))
          case "C-f5" => props.dispatch(FileListSortAction(SortMode.ModificationTime))
          case "C-f6" => props.dispatch(FileListSortAction(SortMode.Size))
          case "C-f7" => props.dispatch(FileListSortAction(SortMode.Unsorted))
          case "C-f8" => props.dispatch(FileListSortAction(SortMode.CreationTime))
          case "C-f9" => props.dispatch(FileListSortAction(SortMode.AccessTime))
          case "C-f12" =>
            setShowSortModes(true)
          case "C-c" =>
            FileListState.currentItem(props.state).foreach { item =>
              val text =
                if (item.name == FileListItem.up.name) props.state.currDir.path
                else path.join(props.state.currDir.path, item.name)
              screen.copyToClipboard(text)
            }
          case "C-r" =>
            props.dispatch(props.actions.updateDir(props.dispatch, props.state.currDir.path))
          case k@("enter" | "C-pageup" | "C-pagedown") =>
            val targetDir: js.UndefOr[FileListItem] = k match {
              case "C-pageup" =>
                if (props.state.currDir.isRoot) {
                  process.stdin.emit("keypress", js.undefined, js.Dynamic.literal(
                    name =
                      if (stackProps.isRight) "r"
                      else "l",
                    ctrl = false,
                    meta = true,
                    shift = false
                  ))
                  js.undefined
                }
                else FileListItem.up
              case _ => FileListState.currentItem(props.state).filter(_.isDir)
            }
            targetDir.foreach { dir =>
              props.dispatch(props.actions.changeDir(
                dispatch = props.dispatch,
                path = props.state.currDir.path,
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
      },

      if (showSortModes) Some(
        <(sortModesPopup)(^.plain := SortModesPopupProps(
          sort = props.state.sort,
          onClose = { () =>
            setShowSortModes(false)
          }
        ))()
      ) else None
    )
  }
}
