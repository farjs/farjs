package scommons.farc.ui.list

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

case class VerticalListProps(items: List[String])

object VerticalList extends FunctionComponent[VerticalListProps] {
  
  protected def render(props: Props): ReactElement = {
    val (focusedIndex, setFocusedIndex) = useState(-1)
    val items = props.wrapped.items
    
    def focusItem(currIndex: Int, currEl: BlessedElement, offset: Int): Unit = {
      val newIndex = currIndex + offset
      if (newIndex != focusedIndex && newIndex >= 0 && newIndex < items.size) {
        currEl.screen.focusOffset(offset)
      }
    }
    
    <.>()(
      items.zipWithIndex.map { case (text, index) =>
        <(ListItem())(
          ^.key := s"$index",
          ^.wrapped := ListItemProps(
            pos = index,
            style = styles.normalItem,
            text = text,
            focused = focusedIndex == index,
            onFocus = { () =>
              setFocusedIndex(index)
            },
            onKeyPress = { (el, key) =>
              key.full match {
                case "up" => focusItem(index, el, - 1)
                case "down" => focusItem(index, el, 1)
                case _ =>
              }
            }
          )
        )()
      }
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
