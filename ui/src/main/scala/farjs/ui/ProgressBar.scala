package farjs.ui

import scommons.react._
import scommons.react.blessed._

object ProgressBar extends FunctionComponent[ProgressBarProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain

    <.text(
      ^.rbWidth := props.length,
      ^.rbHeight := 1,
      ^.rbLeft := props.left,
      ^.rbTop := props.top,
      ^.rbStyle := props.style,
      ^.content := {
        val filledLen = filledLength(props.percent, props.length)

        filledCh * filledLen +
          dottedCh * (props.length - filledLen)
      }
    )()
  }
  
  private[ui] def filledLength(percent: Int, length: Int): Int = {
    (length * percent) / 100
  }

  val filledCh = "\u2588" // █
  val dottedCh = "\u2591" // ░
}
