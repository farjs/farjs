package scommons.farc.ui.filelist

import scommons.react._
import scommons.react.hooks._

case class FileListProps(size: (Int, Int),
                         columns: Int,
                         items: List[(Int, String)])

object FileList extends FunctionComponent[FileListProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val (viewOffset, setViewOffset) = useState(0)
    val (focusedIndex, setFocusedIndex) = useState(-1)
    val (selectedIds, setSelectedIds) = useState(Set.empty[Int])
    
    val props = compProps.wrapped
    val (_, height) = props.size
    val columns = props.columns
    val items = props.items
    val columnSize = height - 1 // excluding column header
    
    val viewSize = columnSize * columns
    val viewItems = items.view(viewOffset, viewOffset + viewSize)
    val maxOffset = items.size - viewItems.size
    val maxIndex = math.max(viewItems.size - 1, 0)

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
      setViewOffset(newOffset)
      
      val newIndex = math.min(math.max(index, 0), maxIndex)
      setFocusedIndex(newIndex)

      if (select && items.nonEmpty) {
        val currIndex = viewOffset + math.min(math.max(focusedIndex, 0), maxIndex)
        val selectIndex = newOffset + newIndex
        
        val isFirst = selectIndex == 0
        val isLast = selectIndex == items.size - 1
        val selectionIds = {
          if (isFirst && selectIndex < currIndex) items.view(selectIndex, currIndex + 1)
          else if (selectIndex < currIndex) items.view(selectIndex + 1, currIndex + 1)
          else if (isLast && selectIndex > currIndex) items.view(currIndex, selectIndex + 1)
          else if (selectIndex > currIndex) items.view(currIndex, selectIndex)
          else items.view(currIndex, selectIndex + 1)
        }.map(_._1).toSet

        val currId = items(currIndex)._1
        val newSelectedIds =
          if (selectedIds.contains(currId)) selectedIds -- selectionIds
          else selectedIds ++ selectionIds

        setSelectedIds(newSelectedIds)
      }
    }
    
    <(FileListView())(^.wrapped := FileListViewProps(
      size = props.size,
      columns = props.columns,
      items = viewItems,
      focusedIndex = focusedIndex,
      selectedIds = selectedIds,
      onWheelUp = { () =>
        if (viewOffset > 0) focusItem(viewOffset - 5, focusedIndex)
        else focusItem(viewOffset, focusedIndex - 5)
      },
      onWheelDown = { () =>
        if (viewOffset < maxOffset) focusItem(viewOffset + 5, focusedIndex)
        else focusItem(viewOffset, focusedIndex + 5)
      },
      onClick = { index =>
        focusItem(viewOffset, index)
      },
      onKeypress = {
        case k if k == "up" || k == "S-up" => focusDx(- 1, k == "S-up")
        case k if k == "down" || k == "S-down" => focusDx(1, k == "S-down")
        case k if k == "left" || k == "S-left" => focusDx(-columnSize, k == "S-left")
        case k if k == "right" || k == "S-right" => focusDx(columnSize, k == "S-right")
        case k if k == "pageup" || k == "S-pageup" => focusDx(-viewSize + 1, k == "S-pageup")
        case k if k == "pagedown" || k == "S-pagedown" => focusDx(viewSize - 1, k == "S-pagedown")
        case k if k == "home" || k == "S-home" => focusItem(0, 0, k == "S-home")
        case k if k == "end" || k == "S-end" => focusItem(maxOffset, maxIndex, k == "S-end")
        case _ =>
      }
    ))()
  }
}
