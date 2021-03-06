package farjs.filelist

import farjs.filelist.api.FileListItem
import farjs.filelist.popups.FileListPopupsActions._
import farjs.ui._
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.nodejs.path
import scommons.react._
import scommons.react.blessed.BlessedScreen

case class FileListPanelProps(dispatch: Dispatch,
                              actions: FileListActions,
                              state: FileListState)

object FileListPanel extends FunctionComponent[FileListPanelProps] {

  private[filelist] var withSizeComp: UiComponent[WithSizeProps] = WithSize
  private[filelist] var fileListPanelView: UiComponent[FileListPanelViewProps] = FileListPanelView

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    def onKeypress(screen: BlessedScreen, key: String): Unit = {
      key match {
        case "f1" => props.dispatch(FileListPopupHelpAction(show = true))
        case "f3" =>
          val currItem = props.state.currentItem.filter(_ != FileListItem.up)
          if (props.state.selectedNames.nonEmpty || currItem.exists(_.isDir)) {
            props.dispatch(FileListPopupViewItemsAction(show = true))
          }
        case "f7" => props.dispatch(FileListPopupMkFolderAction(show = true))
        case "f8" | "delete" =>
          val currItem = props.state.currentItem.filter(_ != FileListItem.up)
          if (props.state.selectedNames.nonEmpty || currItem.isDefined) {
            props.dispatch(FileListPopupDeleteAction(show = true))
          }
        case "f10" => props.dispatch(FileListPopupExitAction(show = true))
        case "tab" => screen.focusNext()
        case "S-tab" => screen.focusPrevious()
        case "C-c" =>
          props.state.currentItem.foreach { item =>
            val text =
              if (item.name == FileListItem.up.name) props.state.currDir.path
              else path.join(props.state.currDir.path, item.name)
            screen.copyToClipboard(text)
          }
        case "M-pagedown" =>
          props.state.currentItem.foreach { item =>
            props.dispatch(props.actions.openInDefaultApp(props.state.currDir.path, item.name))
          }
        case k@("enter" | "C-pageup" | "C-pagedown") =>
          val targetDir = k match {
            case "C-pageup" => Some(FileListItem.up)
            case _ => props.state.currentItem.filter(_.isDir)
          }
          targetDir.foreach { dir =>
            props.dispatch(props.actions.changeDir(
              dispatch = props.dispatch,
              isRight = props.state.isRight,
              parent = Some(props.state.currDir.path),
              dir = dir.name
            ))
          }
        case _ =>
      }
    }
  
    <(withSizeComp())(^.wrapped := WithSizeProps({ (width, height) =>
      <(fileListPanelView())(^.wrapped := FileListPanelViewProps(
        dispatch = props.dispatch,
        actions = props.actions,
        state = props.state,
        width = width,
        height = height,
        onKeypress = onKeypress
      ))()
    }))()
  }
}
