package farjs.filelist.copy

import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._

case class CopyProgressPopupProps(item: String,
                                  to: String,
                                  itemPercent: Int,
                                  total: Double,
                                  totalPercent: Int,
                                  timeSeconds: Int,
                                  leftSeconds: Int,
                                  bytesPerSecond: Double,
                                  onCancel: () => Unit)

object CopyProgressPopup extends FunctionComponent[CopyProgressPopupProps] {

  private[copy] var popupComp: UiComponent[PopupProps] = Popup
  private[copy] var doubleBorderComp: UiComponent[DoubleBorderProps] = DoubleBorder
  private[copy] var textLineComp: UiComponent[TextLineProps] = TextLine
  private[copy] var horizontalLineComp: UiComponent[HorizontalLineProps] = HorizontalLine
  private[copy] var progressBarComp: UiComponent[ProgressBarProps] = ProgressBar

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (width, height) = (50, 13)
    val theme = Theme.current.popup.regular
    
    <(popupComp())(^.wrapped := PopupProps(onClose = props.onCancel))(
      <.box(
        ^.rbClickable := true,
        ^.rbAutoFocus := false,
        ^.rbWidth := width,
        ^.rbHeight := height,
        ^.rbTop := "center",
        ^.rbLeft := "center",
        ^.rbShadow := true,
        ^.rbStyle := theme
      )(
        <(doubleBorderComp())(^.wrapped := DoubleBorderProps(
          size = (width - 6, height - 2),
          style = theme,
          pos = (3, 1),
          title = Some("Copy")
        ))(),

        <.text(
          ^.rbLeft := 5,
          ^.rbTop := 2,
          ^.rbStyle := theme,
          ^.content :=
            """Copying the file
              |
              |to
              |""".stripMargin
        )(),
        <(textLineComp())(^.wrapped := TextLineProps(
          align = TextLine.Left,
          pos = (5, 3),
          width = width - 10,
          text = props.item,
          style = theme,
          padding = 0
        ))(),
        <(textLineComp())(^.wrapped := TextLineProps(
          align = TextLine.Left,
          pos = (5, 5),
          width = width - 10,
          text = props.to,
          style = theme,
          padding = 0
        ))(),

        <(progressBarComp())(^.wrapped := ProgressBarProps(
          percent = props.itemPercent,
          pos = (5, 6),
          length = width - 10,
          style = theme
        ))(),
        <(horizontalLineComp())(^.wrapped := HorizontalLineProps(
          pos = (5, 7),
          length = width - 10,
          lineCh = SingleBorder.horizontalCh,
          style = theme
        ))(),
        <(textLineComp())(^.wrapped := TextLineProps(
          align = TextLine.Center,
          pos = (5, 7),
          width = width - 10,
          text = f"Total: ${props.total}%,.0f",
          style = theme
        ))(),
        <(progressBarComp())(^.wrapped := ProgressBarProps(
          percent = props.totalPercent,
          pos = (5, 8),
          length = width - 10,
          style = theme
        ))(),

        <(horizontalLineComp())(^.wrapped := HorizontalLineProps(
          pos = (5, 9),
          length = width - 10,
          lineCh = SingleBorder.horizontalCh,
          style = theme
        ))(),

        <.text(
          ^.rbLeft := 5,
          ^.rbTop := 10,
          ^.rbStyle := theme,
          ^.content := s"Time: ${toTime(props.timeSeconds)} Left: ${toTime(props.leftSeconds)}"
        )(),
        <(textLineComp())(^.wrapped := TextLineProps(
          align = TextLine.Right,
          pos = (35, 10),
          width = width - 40,
          text = s"${toSpeed(props.bytesPerSecond * 8)}/s",
          style = theme,
          padding = 0
        ))(),

        //for capturing inputs
        <.button(^.rbWidth := 0, ^.rbHeight := 0)()
      )
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
