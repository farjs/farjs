package scommons.farc.ui.list

import scommons.react._
import scommons.react.blessed._

case class ListItemProps(top: Int,
                         style: BlessedStyle,
                         text: String,
                         focused: Boolean)

object ListItem extends FunctionComponent[ListItemProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    
    <.box(
      ^.rbTop := props.top,
      ^.rbHeight := 1,
      ^.rbStyle := {
        if (props.focused) props.style.focus.orNull
        else props.style
      },
      ^.content := props.text
    )()
  }
}
