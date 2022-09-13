package farjs.ui

import scommons.react._
import scommons.react.blessed._

object ScrollBar extends FunctionComponent[ScrollBarProps] {
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val unitIncrement = 1
    val blockIncrement = math.max(props.extent, 1)
    val barLength = math.max(props.length, 2) - 2
    val min = math.max(props.min, 0)
    val max = math.max(props.max, 0)
    val value = math.min(math.max(props.value, min), max)
    val markerLength = 1
    val upLength =
      if (value == min) 0
      else if (value == max) barLength - markerLength
      else {
        val upLen = value * barLength.toDouble / math.max(max - min, 1)
        math.max(math.min(upLen.toInt, barLength - markerLength - 1), 1)
      }
    val downLength = barLength - upLength - markerLength

    <.>()(
      <.text(
        ^.rbWidth := 1,
        ^.rbHeight := 1,
        ^.rbLeft := props.left,
        ^.rbTop := props.top,
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := props.style,
        ^.rbOnClick := { _ =>
          props.onChange(math.max(props.value - unitIncrement, min))
        },
        ^.content := upArrowCh
      )(),
      <.text(
        ^.rbWidth := 1,
        ^.rbHeight := upLength,
        ^.rbLeft := props.left,
        ^.rbTop := props.top + 1,
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := props.style,
        ^.rbOnClick := { _ =>
          props.onChange(math.max(props.value - blockIncrement, min))
        },
        ^.content := scrollCh * upLength
      )(),
      <.text(
        ^.rbWidth := 1,
        ^.rbHeight := markerLength,
        ^.rbLeft := props.left,
        ^.rbTop := props.top + 1 + upLength,
        ^.rbAutoFocus := false,
        ^.rbStyle := props.style,
        ^.content := markerCh
      )(),
      <.text(
        ^.rbWidth := 1,
        ^.rbHeight := downLength,
        ^.rbLeft := props.left,
        ^.rbTop := props.top + 1 + upLength + markerLength,
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := props.style,
        ^.rbOnClick := { _ =>
          props.onChange(math.min(props.value + blockIncrement, max))
        },
        ^.content := scrollCh * downLength
      )(),
      <.text(
        ^.rbWidth := 1,
        ^.rbHeight := 1,
        ^.rbLeft := props.left,
        ^.rbTop := props.top + 1 + upLength + markerLength + downLength,
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := props.style,
        ^.rbOnClick := { _ =>
          props.onChange(math.min(props.value + unitIncrement, max))
        },
        ^.content := downArrowCh
      )()
    )
  }

  private[ui] val markerCh = "\u2588" // █
  private[ui] val scrollCh = "\u2591" // ░

  private[ui] val upArrowCh = "\u25b2" // ▲
  private[ui] val downArrowCh = "\u25bc" // ▼
}
