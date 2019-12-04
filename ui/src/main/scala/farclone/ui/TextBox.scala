package farclone.ui

import scommons.react._
import scommons.react.blessed._

case class TextBoxProps(pos: (Int, Int),
                        width: Int,
                        value: String,
                        style: BlessedStyle,
                        onChange: String => Unit)

object TextBox extends FunctionComponent[TextBoxProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (left, top) = props.pos

    <.input(
      ^.rbMouse := true,
      ^.rbKeyable := true,
      ^.rbWidth := props.width,
      ^.rbHeight := 1,
      ^.rbLeft := left,
      ^.rbTop := top,
      ^.rbStyle := props.style,
      ^.content := props.value
    )()
  }
}
