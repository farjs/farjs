package farjs.ui.popup

import farjs.ui._
import farjs.ui.border._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._

case class StatusPopupProps(text: String,
                            title: String = "Status",
                            closable: Boolean = false,
                            onClose: () => Unit = () => ())

object StatusPopup extends FunctionComponent[StatusPopupProps] {

  private[popup] var popupComp: UiComponent[PopupProps] = Popup
  private[popup] var doubleBorderComp: UiComponent[DoubleBorderProps] = DoubleBorder
  private[popup] var textLineComp: UiComponent[TextLineProps] = TextLine

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val width = 35
    val textWidth = width - 8
    val textLines = UI.splitText(props.text, textWidth - 2) //exclude padding
    val height = 4 + textLines.size
    val theme = Theme.current.popup.regular
    val style = new BlessedStyle {
      override val bold = theme.bold
      override val bg = theme.bg
      override val fg = theme.fg
    }

    <(popupComp())(^.wrapped := PopupProps(
      onClose = props.onClose,
      closable = props.closable
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
          title = Some(props.title)
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
}
