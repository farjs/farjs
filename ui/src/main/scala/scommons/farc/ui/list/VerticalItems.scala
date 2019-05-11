package scommons.farc.ui.list

import scommons.react._
import scommons.react.blessed._

case class VerticalItemsProps(size: (Int, Int),
                              left: Int,
                              boxStyle: BlessedStyle,
                              itemStyle: BlessedStyle,
                              items: Seq[(Int, String)],
                              focusedPos: Int)

object VerticalItems extends FunctionComponent[VerticalItemsProps] {

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
        
        <(ListItem())(
          ^.key := s"$pos",
          ^.wrapped := ListItemProps(
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
