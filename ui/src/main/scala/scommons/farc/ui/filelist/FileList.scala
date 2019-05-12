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
    val props = compProps.wrapped

    val (width, height) = props.size
    val columns = props.columns
    val totalSize = props.items.size
    val viewSize = height * columns
    val items: Seq[(Int, String)] = {
      props.items.view(viewOffset, viewOffset + viewSize)
    }
    val itemsSize = items.size

    def focusItem(index: Int): Unit = {
      if (index < 0 || index >= itemsSize) {
        val offset = index - focusedIndex
        setViewOffset(
          math.min(math.max(viewOffset + offset, 0), totalSize - itemsSize)
        )
      }

      val newIndex =
        if (index <= 0) 0
        else if (index >= itemsSize) itemsSize - 1
        else index
      
      setFocusedIndex(newIndex)
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
      ^.rbMouse := true,
      ^.rbOnClick := { data =>
        val curr = elementRef.current
        val x = data.x - curr.aleft
        val y = data.y - curr.atop
        val colIndex = columnsPos.indexWhere { case (left, len, _) =>
          left <= x && x < (left + len)
        }
        if (colIndex != -1) {
          focusItem(colIndex * height + y)
        }
      },
      ^.rbOnKeypress := { (_, key) =>
        key.full match {
          case "up" => focusItem(focusedIndex - 1)
          case "down" => focusItem(focusedIndex + 1)
          case "left" => focusItem(focusedIndex - height)
          case "right" => focusItem(focusedIndex + height)
          case "pageup" => focusItem(
            if (focusedIndex > 0) 0
            else -viewSize
          )
          case "pagedown" => focusItem(
            if (focusedIndex < itemsSize - 1) itemsSize - 1
            else focusedIndex + viewSize
          )
          case "home" => focusItem(-viewOffset)
          case "end" => focusItem(totalSize - 1)
          case _ =>
        }
      }
    )(
      if (height > 0) {
        val columnsItems = items.grouped(height).toSeq
        columnsItems.zipAll(columnsPos, Nil, (0, 0, 0)).map {
          case (colItems, (colLeft, colWidth, colIndex)) =>
            <.>(^.key := s"$colIndex")(
              if (colIndex != columns - 1) Some(
                <(VerticalLine())(^.wrapped := VerticalLineProps(
                  pos = (colLeft + colWidth, -1),
                  length = height + 2,
                  lineCh = SingleBorder.verticalCh,
                  style = styles.normalItem,
                  startCh = Some(SingleBorder.topCh),
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
                  val firstIndex = height * colIndex
                  val lastIndex = firstIndex + colItems.size - 1
                  if (firstIndex <= focusedIndex && focusedIndex <= lastIndex) {
                    focusedIndex - firstIndex
                  }
                  else -1
                }
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
      override val fg = "white"
      override val bg = "blue"
      override val focus = new BlessedStyle {
        override val fg = "black"
        override val bg = "cyan"
      }
    }
  }
}
