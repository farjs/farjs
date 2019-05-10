package scommons.farc.ui.list

import scommons.react._
import scommons.react.blessed._

case class ListItemProps(width: Int,
                         top: Int,
                         style: BlessedStyle,
                         text: String,
                         focused: Boolean)

object ListItem extends FunctionComponent[ListItemProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val longItem = props.text.length > props.width
    
    <.>()(
      <.text(
        ^.key := "text",
        ^.rbWidth := props.width,
        ^.rbHeight := 1,
        ^.rbTop := props.top,
        ^.rbStyle := {
          if (props.focused) props.style.focus.orNull
          else props.style
        },
        ^.content := props.text
      )(),

      if (longItem) Some(
        <.text(
          ^.key := "longMark",
          ^.rbHeight := 1,
          ^.rbLeft := props.width,
          ^.rbTop := props.top,
          ^.rbStyle := new BlessedStyle {
            override val fg = "red"
            override val bg = props.style.bg
          },
          ^.content := "}"
        )()
      )
      else None
    )
  }
}
