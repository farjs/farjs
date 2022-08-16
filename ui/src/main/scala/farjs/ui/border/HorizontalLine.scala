package farjs.ui.border

import scommons.react._
import scommons.react.blessed._

object HorizontalLine extends FunctionComponent[HorizontalLineProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain

    <.text(
      ^.rbWidth := props.length,
      ^.rbHeight := 1,
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
