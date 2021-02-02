package farjs.ui.filelist

import farjs.api.filelist.FileListItem
import farjs.ui.border._
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

case class FileListViewProps(size: (Int, Int),
                             columns: Int,
                             items: Seq[FileListItem],
                             focusedIndex: Int = -1,
                             selectedNames: Set[String] = Set.empty,
                             onActivate: () => Unit = () => (),
                             onWheel: Boolean => Unit = _ => (),
                             onClick: Int => Unit = _ => (),
                             onKeypress: (BlessedScreen, String) => Unit = (_, _) => ())

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
          props.onKeypress(elementRef.current.screen, keyFull)
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
  
  private[filelist] lazy val styles = new Styles
  private[filelist] class Styles {
    
    val normalItem: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "#008"
      override val fg = "#5ce"
      override val focus = new BlessedStyle {
        override val bold = true
        override val bg = "#088"
        override val fg = "#111"
      }
    }
    val dirItem: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "#008"
      override val fg = "#fff"
      override val focus = new BlessedStyle {
        override val bold = true
        override val bg = "#088"
        override val fg = "#fff"
      }
    }
    val hiddenItem: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "#008"
      override val fg = "#055"
      override val focus = new BlessedStyle {
        override val bold = true
        override val bg = "#088"
        override val fg = "#055"
      }
    }
    val selectedItem: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "#008"
      override val fg = "yellow"
      override val focus = new BlessedStyle {
        override val bold = true
        override val bg = "#088"
        override val fg = "yellow"
      }
    }
    
    val headerStyle: BlessedStyle = new BlessedStyle {
      override val bold = true
      override val bg = "#008"
      override val fg = "yellow"
    }
  }
}
