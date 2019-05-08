package scommons.farc.ui.border

import scommons.react._
import scommons.react.blessed._

case class HorizontalLineProps(pos: (Int, Int),
                               length: Int,
                               lineCh: String,
                               style: BlessedStyle,
                               startCh: Option[String] = None,
                               endCh: Option[String] = None)

object HorizontalLine extends FunctionComponent[HorizontalLineProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (left, top) = props.pos

    <.text(
      ^.rbWidth := props.length,
      ^.rbHeight := 1,
      ^.rbLeft := left,
      ^.rbTop := top,
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
