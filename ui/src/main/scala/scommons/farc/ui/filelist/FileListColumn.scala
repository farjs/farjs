package scommons.farc.ui.filelist

import scommons.react._
import scommons.react.blessed._

case class FileListColumnProps(size: (Int, Int),
                               left: Int,
                               boxStyle: BlessedStyle,
                               itemStyle: BlessedStyle,
                               items: Seq[(Int, String)],
                               focusedPos: Int)

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
      
      items.map { case (_, text) =>
        pos += 1
        
        <(FileListItem())(
          ^.key := s"$pos",
          ^.wrapped := FileListItemProps(
            width = width,
            top = pos,
            style = props.itemStyle,
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
      renderItems(props.items)
    )
  }
}
