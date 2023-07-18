package farjs.ui.popup

import farjs.ui._
import farjs.ui.popup.ModalContent._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._

import scala.scalajs.js

case class StatusPopupProps(text: String,
                            title: String = "Status",
                            closable: Boolean = false,
                            onClose: () => Unit = () => ())

object StatusPopup extends FunctionComponent[StatusPopupProps] {

  private[popup] var popupComp: ReactClass = Popup
  private[popup] var modalContentComp: UiComponent[ModalContentProps] = ModalContent
  private[popup] var textLineComp: ReactClass = TextLine

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val width = 35
    val textWidth = width - (paddingHorizontal + 2) * 2
    val textLines = UI.splitText(props.text, textWidth)
    val height = (paddingVertical + 1) * 2 + textLines.size
    val theme = Theme.useTheme().popup.regular
    val style = new BlessedStyle {
      override val bold = theme.bold
      override val bg = theme.bg
      override val fg = theme.fg
    }

    <(popupComp)(^.plain := PopupProps(
      onClose =
        if (props.closable) props.onClose: js.Function0[Unit]
        else js.undefined
    ))(
      <(modalContentComp())(^.plain := ModalContentProps(
        title = props.title,
        width = width,
        height = height,
        style = style
      ))(
        <.button(
          ^.rbWidth := textWidth,
          ^.rbHeight := textLines.size,
          ^.rbLeft := 2,
          ^.rbTop := 1,
          ^.rbStyle := style
        )(
          textLines.zipWithIndex.map { case (text, index) =>
            <(textLineComp)(^.key := s"$index", ^.plain := TextLineProps(
              align = TextAlign.center,
              left = 0,
              top = index,
              width = textWidth,
              text = text,
              style = style,
              padding = 0
            ))()
          }
        )
      )
    )
  }
}
