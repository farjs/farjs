package farjs.ui.filelist

import farjs.api.filelist._
import farjs.ui.filelist.FileListActions._
import farjs.ui.filelist.popups.FileListPopupsActions._
import io.github.shogowada.scalajs.reactjs.redux.Redux.Dispatch
import scommons.nodejs.path
import scommons.react._
import scommons.react.hooks._

case class FileListProps(dispatch: Dispatch,
                         actions: FileListActions,
                         state: FileListState,
                         size: (Int, Int),
                         columns: Int)

object FileList extends FunctionComponent[FileListProps] {

  private[filelist] var fileListViewComp: UiComponent[FileListViewProps] = FileListView
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    
    val (_, height) = props.size
    val items = props.state.currDir.items
    val columnSize = height - 1 // excluding column header
    val viewSize = columnSize * props.columns
    
    val (viewOffset, focusedIndex) = {
      val offset = math.max(props.state.offset, 0)
      val index = math.max(props.state.index, 0)
      
      if (viewSize <= 0 || index < viewSize) (offset, index)
      else {
        val viewIndex = index % viewSize
        (index - viewIndex, viewIndex)
      }
    }
    
    val viewItems = items.view(viewOffset, viewOffset + viewSize)
    val maxOffset = items.size - viewItems.size
    val maxIndex = math.max(viewItems.size - 1, 0)

    useLayoutEffect({ () =>
      props.dispatch(props.actions.changeDir(
        dispatch = props.dispatch,
        isRight = props.state.isRight,
        parent = None,
        dir = FileListDir.curr
      )): Unit
    }, Nil)
    
    def focusDx(dx: Int, select: Boolean): Unit = {
      val index = focusedIndex + dx
      if (index < 0 || index > maxIndex) {
        val newOffset = viewOffset + dx
        val newIndex =
          if (newOffset < 0) 0
          else if (newOffset > maxOffset) maxIndex
          else focusedIndex

        focusItem(newOffset, newIndex, select)
      }
      else focusItem(viewOffset, index, select)
    }
    
    def focusItem(offset: Int, index: Int, select: Boolean = false): Unit = {
      val newOffset = math.min(math.max(offset, 0), maxOffset)
      val newIndex = math.min(math.max(index, 0), maxIndex)
      
      val currSelected = props.state.selectedNames
      val newSelected =
        if (select && items.nonEmpty) {
          val currIndex = viewOffset + math.min(math.max(focusedIndex, 0), maxIndex)
          val selectIndex = newOffset + newIndex
          
          val isFirst = selectIndex == 0
          val isLast = selectIndex == items.size - 1
          val selection = {
            if (isFirst && selectIndex < currIndex) items.view(selectIndex, currIndex + 1)
            else if (selectIndex < currIndex) items.view(selectIndex + 1, currIndex + 1)
            else if (isLast && selectIndex > currIndex) items.view(currIndex, selectIndex + 1)
            else items.view(currIndex, selectIndex)
          }.map(_.name).toSet
  
          val currName = items(currIndex).name
          val newSelected =
            if (currSelected.contains(currName)) currSelected -- selection
            else currSelected ++ selection
          
          newSelected - FileListItem.up.name
        }
        else currSelected

      if (props.state.offset != newOffset
        || props.state.index != newIndex
        || props.state.selectedNames != newSelected) {
        
        props.dispatch(FileListParamsChangedAction(
          isRight = props.state.isRight,
          offset = newOffset,
          index = newIndex,
          selectedNames = newSelected
        ))
      }
    }
    
    <(fileListViewComp())(^.wrapped := FileListViewProps(
      size = props.size,
      columns = props.columns,
      items = viewItems,
      focusedIndex = if (props.state.isActive) focusedIndex else -1,
      selectedNames = props.state.selectedNames,
      onActivate = { () =>
        if (!props.state.isActive) {
          props.dispatch(FileListActivateAction(props.state.isRight))
        }
      },
      onWheel = { up =>
        if (props.state.isActive) {
          if (up) {
            if (viewOffset > 0) focusItem(viewOffset - 1, focusedIndex)
            else focusItem(viewOffset, focusedIndex - 1)
          }
          else {
            if (viewOffset < maxOffset) focusItem(viewOffset + 1, focusedIndex)
            else focusItem(viewOffset, focusedIndex + 1)
          }
        }
      },
      onClick = { index =>
        focusItem(viewOffset, index)
      },
      onKeypress = {
        case (_, k) if k == "up" || k == "S-up" => focusDx(- 1, k == "S-up")
        case (_, k) if k == "down" || k == "S-down" => focusDx(1, k == "S-down")
        case (_, k) if k == "left" || k == "S-left" => focusDx(-columnSize, k == "S-left")
        case (_, k) if k == "right" || k == "S-right" => focusDx(columnSize, k == "S-right")
        case (_, k) if k == "pageup" || k == "S-pageup" => focusDx(-viewSize + 1, k == "S-pageup")
        case (_, k) if k == "pagedown" || k == "S-pagedown" => focusDx(viewSize - 1, k == "S-pagedown")
        case (_, k) if k == "home" || k == "S-home" => focusItem(0, 0, k == "S-home")
        case (_, k) if k == "end" || k == "S-end" => focusItem(maxOffset, maxIndex, k == "S-end")
        case (screen, "C-c") =>
          props.state.currentItem.foreach { item =>
            val text =
              if (item.name == FileListItem.up.name) props.state.currDir.path
              else path.join(props.state.currDir.path, item.name)
            screen.copyToClipboard(text)
          }
        case (_, "M-pagedown") =>
          props.state.currentItem.foreach { item =>
            props.dispatch(props.actions.openInDefaultApp(props.state.currDir.path, item.name))
          }
        case (_, k@("enter" | "C-pageup" | "C-pagedown")) =>
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
        case (_, "f1") => props.dispatch(FileListPopupHelpAction(show = true))
        case (_, "f7") => props.dispatch(FileListPopupMkFolderAction(show = true))
        case (_, k) if k == "f8" || k == "delete" =>
          if (props.state.selectedNames.nonEmpty
            || props.state.currentItem.exists(_ != FileListItem.up)) {
            props.dispatch(FileListPopupDeleteAction(show = true))
          }
        case (_, "f10") => props.dispatch(FileListPopupExitAction(show = true))
        case _ =>
      }
    ))()
  }
}
