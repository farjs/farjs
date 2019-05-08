package scommons.farc.ui.list

import scommons.farc.ui.border._
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

case class VerticalListProps(size: (Int, Int), columns: Int, items: List[String])

object VerticalList extends FunctionComponent[VerticalListProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val elementRef = useRef[BlessedElement](null)
    val (viewOffset, setViewOffset) = useState(0)
    val (focusedIndex, setFocusedIndex) = useState(-1)
    val props = compProps.wrapped

    val (width, height) = props.size
    val columns = props.columns
    val totalSize = props.items.size
    val viewSize = height * columns
    val items: Seq[String] = {
      props.items.view(viewOffset, viewOffset + viewSize)
    }

    def focusItem(index: Int): Unit = {
      val itemsSize = items.size
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
          case _ =>
        }
      }
    )(
      if (height > 0) {
        val columnsItems = items.zipWithIndex.grouped(height).toSeq
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
                  endCh = Some(DoubleBorder.bottomSingleCh)
                ))()
              )
              else None,

              <(VerticalItems())(^.wrapped := VerticalItemsProps(
                size = (colWidth, height),
                left = colLeft,
                boxStyle = styles.normalItem,
                itemStyle = styles.normalItem,
                items = colItems,
                focusedIndex = focusedIndex
              ))()
            )
        }
      }
      else Nil
    )
  }
  
  private[list] lazy val styles = Styles
  
  private[list] object Styles extends js.Object {
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
