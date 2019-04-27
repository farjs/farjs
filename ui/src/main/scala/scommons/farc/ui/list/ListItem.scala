package scommons.farc.ui.list

import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

case class ListItemProps(pos: Int,
                         style: BlessedStyle,
                         text: String,
                         focused: Boolean,
                         onFocus: () => Unit,
                         onKeyPress: (BlessedElement, KeyboardKey) => Unit)

object ListItem extends FunctionComponent[ListItemProps] {
  
  protected def render(props: Props): ReactElement = {
    val elementRef = useRef[BlessedElement](null)
    val data = props.wrapped
    
    <.button(
      ^.reactRef := elementRef,
      ^.rbTop := data.pos,
      ^.rbHeight := 1,
      ^.rbStyle := data.style,
      ^.rbMouse := true,
      ^.rbOnFocus := data.onFocus,
      ^.rbOnKeypress := { (_, key) =>
        data.onKeyPress(elementRef.current, key)
      },
      ^.content := data.text
    )()
  }
}
