package farjs.app.task

import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup._
import scommons.react._
import scommons.react.blessed._

case class TaskStatusPopupProps(text: String)

object TaskStatusPopup extends FunctionComponent[TaskStatusPopupProps] {

  private[task] var popupComp: UiComponent[PopupProps] = Popup
  private[task] var doubleBorderComp: UiComponent[DoubleBorderProps] = DoubleBorder
  private[task] var textLineComp: UiComponent[TextLineProps] = TextLine

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val width = 35
    val textWidth = width - 8
    val textLines = UI.splitText(props.text, textWidth - 2) //exclude padding
    val height = 4 + textLines.size
    val style = styles.text

    <(popupComp())(^.wrapped := PopupProps(
      onClose = () => (),
      closable = false
    ))(
      <.box(
        ^.rbClickable := true,
        ^.rbAutoFocus := false,
        ^.rbWidth := width,
        ^.rbHeight := height,
        ^.rbTop := "center",
        ^.rbLeft := "center",
        ^.rbShadow := true,
        ^.rbStyle := style
      )(
        <(doubleBorderComp())(^.wrapped := DoubleBorderProps(
          size = (width - 6, height - 2),
          style = style,
          pos = (3, 1),
          title = Some("Status")
        ))(),

        <.button(
          ^.rbWidth := textWidth,
          ^.rbHeight := textLines.size,
          ^.rbLeft := 4,
          ^.rbTop := 2,
          ^.rbStyle := style
        )(
          textLines.zipWithIndex.map { case (text, index) =>
            <(textLineComp())(^.key := s"$index", ^.wrapped := TextLineProps(
              align = TextLine.Center,
              pos = (0, index),
              width = textWidth,
              text = text,
              style = style
            ))()
          }
        )
      )
    )
  }
  
  private[task] lazy val styles = new Styles
  private[task] class Styles {

    val text: BlessedStyle = new BlessedStyle {
      override val bold = Popup.Styles.normal.bold
      override val bg = Popup.Styles.normal.bg
      override val fg = Popup.Styles.normal.fg
    }
  }
}
