package farjs.filelist

import farjs.filelist.api.FileListItem
import farjs.filelist.stack.WithStack
import farjs.filelist.theme.FileListTheme
import farjs.ui.border._
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

case class FileListViewProps(size: (Int, Int),
                             columns: Int,
                             items: Seq[FileListItem],
                             focusedIndex: Int = -1,
                             selectedNames: Set[String] = Set.empty,
                             onWheel: Boolean => Unit = _ => (),
                             onClick: Int => Unit = _ => (),
                             onKeypress: (BlessedScreen, String) => Unit = (_, _) => ())

object FileListView extends FunctionComponent[FileListViewProps] {

  private[filelist] var verticalLineComp: ReactClass = VerticalLine
  private[filelist] var fileListColumnComp: UiComponent[FileListColumnProps] = FileListColumn
  
  protected def render(compProps: Props): ReactElement = {
    val elementRef = useRef[BlessedElement](null)
    val propsRef = useRef[FileListViewProps](null)
    val columnSizeRef = useRef[Int](0)
    val columnsPosRef = useRef[Seq[(Int, Int, Int)]](null)
    val inputEl = WithStack.useStack().panelInput
    val currTheme = FileListTheme.useTheme()
    
    val props = compProps.wrapped
    propsRef.current = props

    val (width, height) = props.size
    val columns = props.columns
    columnSizeRef.current = height - 1 // excluding column header
    
    columnsPosRef.current = useMemo[Seq[(Int, Int, Int)]]({ () =>
      (0 until columns).map { colIndex =>
        val colWidth = width / columns
        val colLeft = colIndex * colWidth
        val finalWidth =
          if (colIndex == columns - 1) width - colLeft
          else colWidth - 1

        (colLeft, finalWidth, colIndex)
      }
    }, List(width, columns))
    
    useLayoutEffect({ () =>
      if (inputEl != null) {
        val keyListener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
          propsRef.current.onKeypress(inputEl.screen, key.full)
        }
        val wheelupListener: js.Function1[MouseData, Unit] = { data =>
          if (!data.shift) {
            propsRef.current.onWheel(true)
          }
        }
        val wheeldownListener: js.Function1[MouseData, Unit] = { data =>
          if (!data.shift) {
            propsRef.current.onWheel(false)
          }
        }
        val clickListener: js.Function1[MouseData, Unit] = { data =>
          val curr = elementRef.current
          val x = data.x - curr.aleft
          val y = data.y - curr.atop
          val colIndex = columnsPosRef.current.indexWhere { case (left, len, _) =>
            left <= x && x < (left + len)
          }
          if (colIndex != -1) {
            val itemPos = if (y > 0) y - 1 else y // exclude column header
            propsRef.current.onClick(colIndex * columnSizeRef.current + itemPos)
          }
        }

        inputEl.on("keypress", keyListener)
        inputEl.on("wheelup", wheelupListener)
        inputEl.on("wheeldown", wheeldownListener)
        inputEl.on("click", clickListener)

        val cleanup: js.Function0[Unit] = { () =>
          inputEl.off("keypress", keyListener)
          inputEl.off("wheelup", wheelupListener)
          inputEl.off("wheeldown", wheeldownListener)
          inputEl.off("click", clickListener)
        }
        cleanup
      }
      else ()
    }, List(inputEl))
    
    <.box(
      ^.reactRef := elementRef,
      ^.rbWidth := width,
      ^.rbHeight := height,
      ^.rbLeft := 1,
      ^.rbTop := 1
    )(
      if (columnSizeRef.current > 0) {
        val columnsItems = props.items.grouped(columnSizeRef.current).toSeq
        
        columnsItems.zipAll(columnsPosRef.current, Nil, (0, 0, 0)).map {
          case (colItems, (colLeft, colWidth, colIndex)) =>
            <.>(^.key := s"$colIndex")(
              if (colIndex != columns - 1) {
                Some(
                  <(verticalLineComp)(^.plain := VerticalLineProps(
                    left = colLeft + colWidth,
                    top = -1,
                    length = height + 2,
                    lineCh = SingleChars.vertical,
                    style = currTheme.fileList.regularItem,
                    startCh = DoubleChars.topSingle,
                    endCh = SingleChars.bottom
                  ))()
                )
              }
              else None,

              <(fileListColumnComp())(^.plain := FileListColumnProps(
                width = colWidth,
                height = height,
                left = colLeft,
                borderCh =
                  if (colIndex != columns - 1) SingleChars.vertical
                  else DoubleChars.vertical,
                items = js.Array(colItems: _*),
                focusedIndex = {
                  val firstIndex = columnSizeRef.current * colIndex
                  val lastIndex = firstIndex + colItems.size - 1
                  val focusedIndex = props.focusedIndex
                  if (firstIndex <= focusedIndex && focusedIndex <= lastIndex) {
                    focusedIndex - firstIndex
                  }
                  else -1
                },
                selectedNames = js.Set(props.selectedNames.intersect(colItems.map(_.name).toSet).toList: _*)
              ))()
            )
        }
      }
      else Nil
    )
  }
}
