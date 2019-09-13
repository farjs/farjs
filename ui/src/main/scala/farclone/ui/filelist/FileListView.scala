package farclone.ui.filelist

import farclone.api.filelist.FileListItem
import farclone.ui.border._
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

case class FileListViewProps(size: (Int, Int),
                             columns: Int,
                             items: Seq[FileListItem],
                             focusedIndex: Int = -1,
                             selectedNames: Set[String] = Set.empty,
                             onActivate: () => Unit = () => (),
                             onWheel: Boolean => Unit = _ => (),
                             onClick: Int => Unit = _ => (),
                             onKeypress: String => Unit = _ => ())

object FileListView extends FunctionComponent[FileListViewProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val elementRef = useRef[BlessedElement](null)
    
    val props = compProps.wrapped

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
    
    useLayoutEffect({ () =>
      if (props.focusedIndex != -1) {
        elementRef.current.focus()
      }
    }, Nil)
    
    <.button(
      ^.reactRef := elementRef,
      ^.rbWidth := width,
      ^.rbHeight := height,
      ^.rbLeft := 1,
      ^.rbTop := 1,
      ^.rbMouse := true,
      ^.rbOnFocus := { () =>
        props.onActivate()
      },
      ^.rbOnWheelup := { data =>
        if (!data.shift) {
          props.onWheel(true)
        }
      },
      ^.rbOnWheeldown := { data =>
        if (!data.shift) {
          props.onWheel(false)
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
        val keyFull = key.full
        if (keyFull == "tab") elementRef.current.screen.focusNext()
        else if (keyFull == "S-tab") elementRef.current.screen.focusPrevious()
        else {
          props.onKeypress(keyFull)
        }
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
                borderCh =
                  if (colIndex != columns - 1) SingleBorder.verticalCh
                  else DoubleBorder.verticalCh,
                items = colItems,
                focusedIndex = {
                  val firstIndex = columnSize * colIndex
                  val lastIndex = firstIndex + colItems.size - 1
                  val focusedIndex = props.focusedIndex
                  if (firstIndex <= focusedIndex && focusedIndex <= lastIndex) {
                    focusedIndex - firstIndex
                  }
                  else -1
                },
                selectedNames = props.selectedNames.intersect(colItems.map(_.name).toSet)
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
    val dirItem: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "blue"
      override val fg = "white"
      override val focus = new BlessedStyle {
        override val bold = true
        override val bg = "cyan"
        override val fg = "white"
      }
    }
    val hiddenItem: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "blue"
      override val fg = "black"
      override val focus = new BlessedStyle {
        override val bold = true
        override val bg = "cyan"
        override val fg = "black"
      }
    }
    val selectedItem: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "blue"
      override val fg = "yellow"
      override val focus = new BlessedStyle {
        override val bold = true
        override val bg = "cyan"
        override val fg = "yellow"
      }
    }
    
    val overlapColor = "red"

    val headerStyle: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "blue"
      override val fg = "yellow"
    }
  }
}
