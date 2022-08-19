package farjs.ui.border

import scommons.react._
import scommons.react.blessed._

object VerticalLine extends FunctionComponent[VerticalLineProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain

    <.text(
      ^.rbWidth := 1,
      ^.rbHeight := props.length,
      ^.rbLeft := props.left,
      ^.rbTop := props.top,
      ^.rbStyle := props.style,
      ^.content := {
        val startCh = props.startCh.getOrElse("")
        val endCh = props.endCh.getOrElse("")

        startCh +
          props.lineCh * (props.length - startCh.length - endCh.length) +
          endCh
      }
    )()
  }
}
