package scommons.farc.ui.filelist

import scommons.farc.ui._
import scommons.react._
import scommons.react.blessed._

case class FileListColumnProps(size: (Int, Int),
                               left: Int,
                               boxStyle: BlessedStyle,
                               itemStyle: BlessedStyle,
                               items: Seq[(Int, String)],
                               focusedPos: Int,
                               selectedIds: Set[Int])

object FileListColumn extends FunctionComponent[FileListColumnProps] {

  override protected def create(): ReactClass = {
    ReactMemo[Props](super.create(), { (prevProps, nextProps) =>
      prevProps.wrapped == nextProps.wrapped
    })
  }
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (width, height) = props.size

    def renderItems(items: Seq[(Int, String)]): Seq[ReactElement] = {
      var pos = -1
      
      items.map { case (id, text) =>
        pos += 1
        
        <(FileListItem())(
          ^.key := s"$pos",
          ^.wrapped := FileListItemProps(
            width = width,
            top = pos + 1,
            style =
              if (props.selectedIds.contains(id)) selectedItem
              else props.itemStyle,
            text = text,
            focused = props.focusedPos == pos
          )
        )()
      }
    }

    <.box(
      ^.rbWidth := width,
      ^.rbHeight := height,
      ^.rbLeft := props.left,
      ^.rbStyle := props.boxStyle
    )(
      <(TextLine())(^.wrapped := TextLineProps(
        align = TextLine.Center,
        pos = (0, 0),
        width = width,
        text = "Name",
        style = headerStyle,
        padding = 0
      ))(),
      
      renderItems(props.items)
    )
  }

  private[filelist] val headerStyle = new BlessedStyle {
    override val bold = true
    override val bg = "blue"
    override val fg = "yellow"
  }

  private[filelist] val selectedItem = new BlessedStyle {
    override val bold = true
    override val bg = "blue"
    override val fg = "yellow"
    override val focus = new BlessedStyle {
      override val bold = true
      override val bg = "cyan"
      override val fg = "yellow"
    }
  }
}
