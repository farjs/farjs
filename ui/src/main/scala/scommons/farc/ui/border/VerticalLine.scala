package scommons.farc.ui.border

import scommons.react._
import scommons.react.blessed._

case class VerticalLineProps(pos: (Int, Int),
                             length: Int,
                             ch: String,
                             style: BlessedStyle,
                             start: Option[String] = None,
                             end: Option[String] = None)

object VerticalLine extends FunctionComponent[VerticalLineProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (left, top) = props.pos

    <.text(
      ^.rbWidth := 1,
      ^.rbHeight := props.length,
      ^.rbLeft := left,
      ^.rbTop := top,
      ^.rbStyle := props.style,
      ^.content := {
        val startCh = props.start.getOrElse("")
        val endCh = props.end.getOrElse("")

        startCh +
          props.ch * (props.length - startCh.length - endCh.length) +
          endCh
      }
    )()
  }
}
