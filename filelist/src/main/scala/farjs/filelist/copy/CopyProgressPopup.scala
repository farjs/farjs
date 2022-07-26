package farjs.filelist.copy

import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._

case class CopyProgressPopupProps(move: Boolean,
                                  item: String,
                                  to: String,
                                  itemPercent: Int,
                                  total: Double,
                                  totalPercent: Int,
                                  timeSeconds: Int,
                                  leftSeconds: Int,
                                  bytesPerSecond: Double,
                                  onCancel: () => Unit)

object CopyProgressPopup extends FunctionComponent[CopyProgressPopupProps] {

  private[copy] var modalComp: UiComponent[ModalProps] = Modal
  private[copy] var textLineComp: UiComponent[TextLineProps] = TextLine
  private[copy] var horizontalLineComp: UiComponent[HorizontalLineProps] = HorizontalLine
  private[copy] var progressBarComp: UiComponent[ProgressBarProps] = ProgressBar

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val size@(width, _) = (50, 13)
    val contentWidth = width - (paddingHorizontal + 2) * 2
    val contentLeft = 2
    val theme = Theme.current.popup.regular
    
    <(modalComp())(^.wrapped := ModalProps(if (props.move) "Move" else "Copy", size, theme, props.onCancel))(
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
      <(textLineComp())(^.wrapped := TextLineProps(
        align = TextLine.Left,
        pos = (contentLeft, 2),
        width = contentWidth,
        text = props.item,
        style = theme,
        padding = 0
      ))(),
      <(textLineComp())(^.wrapped := TextLineProps(
        align = TextLine.Left,
        pos = (contentLeft, 4),
        width = contentWidth,
        text = props.to,
        style = theme,
        padding = 0
      ))(),

      <(progressBarComp())(^.plain := ProgressBarProps(
        percent = props.itemPercent,
        left = contentLeft,
        top = 5,
        length = contentWidth,
        style = theme
      ))(),
      <(horizontalLineComp())(^.wrapped := HorizontalLineProps(
        pos = (contentLeft, 6),
        length = contentWidth,
        lineCh = SingleBorder.horizontalCh,
        style = theme
      ))(),
      <(textLineComp())(^.wrapped := TextLineProps(
        align = TextLine.Center,
        pos = (contentLeft, 6),
        width = contentWidth,
        text = f"Total: ${props.total}%,.0f",
        style = theme
      ))(),
      <(progressBarComp())(^.plain := ProgressBarProps(
        percent = props.totalPercent,
        left = contentLeft,
        top = 7,
        length = contentWidth,
        style = theme
      ))(),

      <(horizontalLineComp())(^.wrapped := HorizontalLineProps(
        pos = (contentLeft, 8),
        length = contentWidth,
        lineCh = SingleBorder.horizontalCh,
        style = theme
      ))(),

      <.text(
        ^.rbLeft := contentLeft,
        ^.rbTop := 9,
        ^.rbStyle := theme,
        ^.content := s"Time: ${toTime(props.timeSeconds)} Left: ${toTime(props.leftSeconds)}"
      )(),
      <(textLineComp())(^.wrapped := TextLineProps(
        align = TextLine.Right,
        pos = (contentLeft + 30, 9),
        width = contentWidth - 30,
        text = s"${toSpeed(props.bytesPerSecond * 8)}/s",
        style = theme,
        padding = 0
      ))(),

      //for capturing inputs
      <.button(^.rbWidth := 0, ^.rbHeight := 0)()
    )
  }
  
  private[copy] def toTime(seconds: Int): String = {
    val hrs = seconds / 3600
    val min = (seconds - hrs * 3600) / 60
    val sec = seconds - hrs * 3600 - min * 60
    
    f"$hrs%02d:$min%02d:$sec%02d"
  }
  
  private[copy] def toSpeed(bits: Double): String = {
    val (speed, mod) =
      if (bits >= 100000000000d) (bits / 1000000000, "Gb")
      else if (bits >= 100000000) (bits / 1000000, "Mb")
      else if (bits >= 100000) (bits / 1000, "Kb")
      else (bits, "b")
    
    f"$speed%.0f$mod"
  }
}
