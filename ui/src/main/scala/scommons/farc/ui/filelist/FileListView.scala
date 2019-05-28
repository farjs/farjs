package scommons.farc.ui.filelist

import scommons.farc.ui.border._
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

case class FileListViewProps(size: (Int, Int),
                             columns: Int,
                             items: Seq[(Int, String)],
                             focusedIndex: Int = -1,
                             selectedIds: Set[Int] = Set.empty,
                             onWheelUp: () => Unit = () => (),
                             onWheelDown: () => Unit = () => (),
                             onClick: Int => Unit = _ => (),
                             onKeypress: String => Unit = _ => ())

object FileListView extends FunctionComponent[FileListViewProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val elementRef = useRef[BlessedElement](null)
    
    val props = compProps.wrapped
    val focusedIndex = props.focusedIndex
    val selectedIds = props.selectedIds

    val (width, height) = props.size
    val columns = props.columns
    val columnSize = height - 1 // excluding column header
    
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
          props.onWheelUp()
        }
      },
      ^.rbOnWheeldown := { data =>
        if (!data.shift) {
          props.onWheelDown()
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
          props.onClick(colIndex * columnSize + itemPos)
        }
      },
      ^.rbOnKeypress := { (_, key) =>
        props.onKeypress(key.full)
      }
    )(
      if (columnSize > 0) {
        val columnsItems = props.items.grouped(columnSize).toSeq
        
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
