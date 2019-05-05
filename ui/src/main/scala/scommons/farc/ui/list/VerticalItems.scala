package scommons.farc.ui.list

import scommons.react._
import scommons.react.blessed._

case class VerticalItemsProps(size: (Int, Int),
                              left: Int,
                              boxStyle: BlessedStyle,
                              itemStyle: BlessedStyle,
                              items: Seq[(String, Int)],
                              focusedIndex: Int)

object VerticalItems extends FunctionComponent[VerticalItemsProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped

    def renderItems(items: Seq[(String, Int)]): Seq[ReactElement] = {
      items.zipWithIndex.map { case ((text, index), top) =>
        <(ListItem())(
          ^.key := s"$top",
          ^.wrapped := ListItemProps(
            top = top,
            style = props.itemStyle,
            text = text,
            focused = props.focusedIndex == index
          )
        )()
      }
    }
    
    val (width, height) = props.size

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
