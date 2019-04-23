package scommons.farc.ui.list

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.scalajs.js

case class VerticalListProps(items: List[String])

object VerticalList extends FunctionComponent[VerticalListProps] {
  
  protected def render(props: Props): ReactElement = {
    val (selectedIndex, setSelectedIndex) = useState(0)
    val items = props.wrapped.items
    
    def setSelected(index: Int): Unit = {
      if (index != selectedIndex && index >= 0 && index < items.size) {
        setSelectedIndex(index)
      }
    }
    
    def renderItem(text: String, index: Int): ReactElement = {
      val isSelected = selectedIndex == index
      
      <.button(
        ^.key := s"$index",
        ^.rbTop := index,
        ^.rbHeight := 1,
        ^.rbStyle := (if (isSelected) styles.selectedItem else styles.normalItem),
        ^.rbMouse := true,
        ^.rbOnClick := { () =>
          setSelected(index)
        },
        ^.rbOnKeyPress := { (_, key) =>
          s"${key.name}" match {
            case "up" => setSelected(selectedIndex - 1)
            case "down" => setSelected(selectedIndex + 1)
            case _ =>
          }
        },
        ^.content := text
      )()
    }

    <.>()(
      items.zipWithIndex.map { case (text, index) =>
        renderItem(text, index)
      }
    )
  }
  
  private[list] lazy val styles = Styles
  
  private[list] object Styles extends js.Object {
    val normalItem: BlessedStyle = new BlessedStyle {
      override val fg = "white"
      override val bg = "blue"
    }
    val selectedItem: BlessedStyle = new BlessedStyle {
      override val fg = "black"
      override val bg = "cyan"
    }
  }
}
