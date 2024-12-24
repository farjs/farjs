package farjs.filelist

import farjs.filelist.FileListActions._
import farjs.filelist.api.FileListItem
import farjs.ui.Dispatch
import scommons.react._
import scommons.react.blessed.BlessedScreen
import scommons.react.hooks._

import scala.scalajs.js

case class FileListProps(dispatch: Dispatch,
                         actions: FileListActions,
                         state: FileListState,
                         size: (Int, Int),
                         columns: Int,
                         onKeypress: (BlessedScreen, String) => Unit = (_, _) => ())

object FileList extends FunctionComponent[FileListProps] {

  private[filelist] var fileListViewComp: ReactClass = FileListView
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    
    val (_, height) = props.size
    val items = props.state.currDir.items.toSeq
    val itemsLength = items.length
    val columnSize = height - 1 // excluding column header
    val viewSize = columnSize * props.columns
    
    val (viewOffset, focusedIndex) = {
      val offset = math.max(props.state.offset, 0)
      val index = math.max(props.state.index, 0)
      
      if (viewSize <= 0 || index < viewSize) (offset, index)
      else {
        val currIndex = offset + index
        val rawOffset = (currIndex / viewSize) * viewSize
        val newOffset = math.max(math.min(itemsLength - viewSize, rawOffset), 0)
        val focused = math.max(math.min(itemsLength - newOffset - 1, currIndex - newOffset), 0)
        (newOffset, focused)
      }
    }
    
    val viewItems = items.slice(viewOffset, viewOffset + viewSize)
    val maxOffset = itemsLength - viewItems.size
    val maxIndex = math.max(viewItems.size - 1, 0)

    useLayoutEffect({ () =>
      if (props.state.currDir.path.isEmpty) {
        props.dispatch(props.actions.changeDir(
          dispatch = props.dispatch,
          path = "",
          dir = FileListItem.currDir.name
        ))
      }
      ()
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
      
      val currSelected = props.state.selectedNames.toSet
      val newSelected =
        if (select && items.nonEmpty) {
          val currIndex = viewOffset + math.min(math.max(focusedIndex, 0), maxIndex)
          val selectIndex = newOffset + newIndex
          
          val isFirst = selectIndex == 0
          val isLast = selectIndex == itemsLength - 1
          val selection = {
            if (isFirst && (selectIndex == currIndex || selectIndex + 1 < currIndex)) {
              items.view.slice(selectIndex, currIndex + 1)
            }
            else if (selectIndex < currIndex) items.view.slice(selectIndex + 1, currIndex + 1)
            else if (isLast && (selectIndex == currIndex || selectIndex > currIndex + 1)) {
              items.view.slice(currIndex, selectIndex + 1)
            }
            else items.view.slice(currIndex, selectIndex)
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
        || currSelected != newSelected) {
        
        props.dispatch(FileListParamsChangedAction(
          offset = newOffset,
          index = newIndex,
          selectedNames = js.Set[String](newSelected.toSeq: _*)
        ))
      }
    }
    
    val (viewWidth, viewHeight) = props.size
    
    <(fileListViewComp)(^.plain := FileListViewProps(
      width = viewWidth,
      height = viewHeight,
      columns = props.columns,
      items = js.Array(viewItems: _*),
      focusedIndex = if (props.state.isActive) focusedIndex else -1,
      selectedNames = props.state.selectedNames,
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
      onKeypress = { (screen, key) =>
        key match {
          case k@("up" | "S-up") => focusDx(-1, k == "S-up")
          case k@("down" | "S-down") => focusDx(1, k == "S-down")
          case k@("left" | "S-left") => focusDx(-columnSize, k == "S-left")
          case k@("right" | "S-right") => focusDx(columnSize, k == "S-right")
          case k@("pageup" | "S-pageup") => focusDx(-viewSize + 1, k == "S-pageup")
          case k@("pagedown" | "S-pagedown") => focusDx(viewSize - 1, k == "S-pagedown")
          case k@("home" | "S-home") => focusItem(0, 0, k == "S-home")
          case k@("end" | "S-end") => focusItem(maxOffset, maxIndex, k == "S-end")
          case _ =>
        }
        
        props.onKeypress(screen, key)
      }
    ))()
  }
}
