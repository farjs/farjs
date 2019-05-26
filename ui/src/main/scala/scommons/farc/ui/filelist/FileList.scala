package scommons.farc.ui.filelist

import scommons.farc.ui.border._
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

case class FileListProps(size: (Int, Int),
                         columns: Int,
                         items: List[(Int, String)])

object FileList extends FunctionComponent[FileListProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val elementRef = useRef[BlessedElement](null)
    val (viewOffset, setViewOffset) = useState(0)
    val (focusedIndex, setFocusedIndex) = useState(-1)
    val (selectedIds, setSelectedIds) = useState(Set.empty[Int])
    val props = compProps.wrapped

    val (width, height) = props.size
    val columns = props.columns
    val totalSize = props.items.size
    val columnSize = height - 1 // excluding column header
    val viewSize = columnSize * columns
    val items: Seq[(Int, String)] = {
      props.items.view(viewOffset, viewOffset + viewSize)
    }
    val maxOffset = totalSize - items.size
    val maxIndex = math.max(items.size - 1, 0)

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

      if (select && props.items.nonEmpty) {
        val currIndex = viewOffset + math.min(math.max(focusedIndex, 0), maxIndex)
        val selectIndex = newOffset + newIndex
        
        val isFirst = selectIndex == 0
        val isLast = selectIndex == totalSize - 1
        val selectionIds = {
          if (isFirst && selectIndex < currIndex) props.items.view(selectIndex, currIndex + 1)
          else if (selectIndex < currIndex) props.items.view(selectIndex + 1, currIndex + 1)
          else if (isLast && selectIndex > currIndex) props.items.view(currIndex, selectIndex + 1)
          else if (selectIndex > currIndex) props.items.view(currIndex, selectIndex)
          else props.items.view(currIndex, selectIndex + 1)
        }.map(_._1).toSet

        val currId = props.items(currIndex)._1
        val newSelectedIds =
          if (selectedIds.contains(currId)) selectedIds -- selectionIds
          else selectedIds ++ selectionIds

        setSelectedIds(newSelectedIds)
      }
    }
    
    val columnsPos: Seq[(Int, Int, Int)] = (0 until columns).map { colIndex =>
      val colWidth = width / columns
      val colLeft = colIndex * colWidth
      val finalWidth =
        if (colIndex == columns - 1) width - colLeft
        else colWidth - 1
      
      (colLeft, finalWidth, colIndex)
    }
    
    <.button(
      ^.reactRef := elementRef,
      ^.rbWidth := width,
      ^.rbHeight := height,
      ^.rbLeft := 1,
      ^.rbTop := 1,
      ^.rbMouse := true,
      ^.rbOnWheelup := { data =>
        if (!data.shift) {
          if (viewOffset > 0) focusItem(viewOffset - 5, focusedIndex)
          else focusItem(viewOffset, focusedIndex - 5)
        }
      },
      ^.rbOnWheeldown := { data =>
        if (!data.shift) {
          if (viewOffset < maxOffset) focusItem(viewOffset + 5, focusedIndex)
          else focusItem(viewOffset, focusedIndex + 5)
        }
      },
      ^.rbOnClick := { data =>
        val curr = elementRef.current
        val x = data.x - curr.aleft
        val y = data.y - curr.atop
        val colIndex = columnsPos.indexWhere { case (left, len, _) =>
          left <= x && x < (left + len)
        }
        if (colIndex != -1) {
          val itemPos = if (y > 0) y - 1 else y // exclude column header
          focusItem(viewOffset, colIndex * columnSize + itemPos)
        }
      },
      ^.rbOnKeypress := { (_, key) =>
        key.full match {
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
      }
    )(
      if (columnSize > 0) {
        val columnsItems = items.grouped(columnSize).toSeq
        columnsItems.zipAll(columnsPos, Nil, (0, 0, 0)).map {
          case (colItems, (colLeft, colWidth, colIndex)) =>
            <.>(^.key := s"$colIndex")(
              if (colIndex != columns - 1) Some(
                <(VerticalLine())(^.wrapped := VerticalLineProps(
                  pos = (colLeft + colWidth, -1),
                  length = height + 2,
                  lineCh = SingleBorder.verticalCh,
                  style = styles.normalItem,
                  startCh = Some(DoubleBorder.topSingleCh),
                  endCh = Some(SingleBorder.bottomCh)
                ))()
              )
              else None,

              <(FileListColumn())(^.wrapped := FileListColumnProps(
                size = (colWidth, height),
                left = colLeft,
                boxStyle = styles.normalItem,
                itemStyle = styles.normalItem,
                items = colItems,
                focusedPos = {
                  val firstIndex = columnSize * colIndex
                  val lastIndex = firstIndex + colItems.size - 1
                  if (firstIndex <= focusedIndex && focusedIndex <= lastIndex) {
                    focusedIndex - firstIndex
                  }
                  else -1
                },
                selectedIds = selectedIds.intersect(colItems.map(_._1).toSet)
              ))()
            )
        }
      }
      else Nil
    )
  }
  
  private[filelist] lazy val styles = Styles
  
  private[filelist] object Styles extends js.Object {
    val normalItem: BlessedStyle = new BlessedStyle {
      override val bold = false
      override val bg = "blue"
      override val fg = "white"
      override val focus = new BlessedStyle {
        override val bold = false
        override val bg = "cyan"
        override val fg = "black"
      }
    }
  }
}
