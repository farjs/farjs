package farjs.copymove

import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._

object CopyProgressPopup extends FunctionComponent[CopyProgressPopupProps] {

  private[copymove] var modalComp: ReactClass = Modal
  private[copymove] var textLineComp: ReactClass = TextLine
  private[copymove] var horizontalLineComp: ReactClass = HorizontalLine
  private[copymove] var progressBarComp: ReactClass = ProgressBar

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val (width, height) = (50, 13)
    val contentWidth = width - (paddingHorizontal + 2) * 2
    val contentLeft = 2
    val theme = Theme.useTheme().popup.regular
    
    <(modalComp)(^.plain := ModalProps(if (props.move) "Move" else "Copy", width, height, theme, props.onCancel))(
      <.text(
        ^.rbLeft := contentLeft,
        ^.rbTop := 1,
        ^.rbStyle := theme,
        ^.content :=
          s"""${if (props.move) "Moving" else "Copying"} the file
             |
             |to
             |""".stripMargin
      )(),
      <(textLineComp)(^.plain := TextLineProps(
        align = TextAlign.left,
        left = contentLeft,
        top = 2,
        width = contentWidth,
        text = props.item,
        style = theme,
        padding = 0
      ))(),
      <(textLineComp)(^.plain := TextLineProps(
        align = TextAlign.left,
        left = contentLeft,
        top = 4,
        width = contentWidth,
        text = props.to,
        style = theme,
        padding = 0
      ))(),

      <(progressBarComp)(^.plain := ProgressBarProps(
        percent = props.itemPercent,
        left = contentLeft,
        top = 5,
        length = contentWidth,
        style = theme
      ))(),
      <(horizontalLineComp)(^.plain := HorizontalLineProps(
        left = contentLeft,
        top = 6,
        length = contentWidth,
        lineCh = SingleChars.horizontal,
        style = theme
      ))(),
      <(textLineComp)(^.plain := TextLineProps(
        align = TextAlign.center,
        left = contentLeft,
        top = 6,
        width = contentWidth,
        text = f"Total: ${props.total}%,.0f",
        style = theme
      ))(),
      <(progressBarComp)(^.plain := ProgressBarProps(
        percent = props.totalPercent,
        left = contentLeft,
        top = 7,
        length = contentWidth,
        style = theme
      ))(),

      <(horizontalLineComp)(^.plain := HorizontalLineProps(
        left = contentLeft,
        top = 8,
        length = contentWidth,
        lineCh = SingleChars.horizontal,
        style = theme
      ))(),

      <.text(
        ^.rbLeft := contentLeft,
        ^.rbTop := 9,
        ^.rbStyle := theme,
        ^.content := s"Time: ${toTime(props.timeSeconds)} Left: ${toTime(props.leftSeconds)}"
      )(),
      <(textLineComp)(^.plain := TextLineProps(
        align = TextAlign.right,
        left = contentLeft + 30,
        top = 9,
        width = contentWidth - 30,
        text = s"${toSpeed(props.bytesPerSecond * 8)}/s",
        style = theme,
        padding = 0
      ))(),

      //for capturing inputs
      <.button(^.rbWidth := 0, ^.rbHeight := 0)()
    )
  }
  
  private[copymove] def toTime(seconds: Int): String = {
    val hrs = seconds / 3600
    val min = (seconds - hrs * 3600) / 60
    val sec = seconds - hrs * 3600 - min * 60
    
    f"$hrs%02d:$min%02d:$sec%02d"
  }
  
  private[copymove] def toSpeed(bits: Double): String = {
    val (speed, mod) =
      if (bits >= 100000000000d) (bits / 1000000000, "Gb")
      else if (bits >= 100000000) (bits / 1000000, "Mb")
      else if (bits >= 100000) (bits / 1000, "Kb")
      else (bits, "b")
    
    f"$speed%.0f$mod"
  }
}
