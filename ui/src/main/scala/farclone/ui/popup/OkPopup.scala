package farclone.ui.popup

import farclone.ui._
import farclone.ui.border._
import scommons.react._
import scommons.react.blessed._

case class OkPopupProps(title: String,
                        message: String,
                        style: BlessedStyle = Popup.Styles.normal,
                        onClose: () => Unit = () => ())

object OkPopup extends FunctionComponent[OkPopupProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (width, height) = (60, 6)

    <(Popup())(^.wrapped := PopupProps(onClose = props.onClose))(
      <.box(
        ^.rbClickable := true,
        ^.rbAutoFocus := false,
        ^.rbWidth := width,
        ^.rbHeight := height,
        ^.rbTop := "center",
        ^.rbLeft := "center",
        ^.rbShadow := true,
        ^.rbStyle := props.style
      )(
        <(DoubleBorder())(^.wrapped := DoubleBorderProps(
          size = (width - 6, height - 2),
          style = props.style,
          pos = (3, 1))
        )(),
        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Center,
          pos = (3, 1),
          width = width - 6,
          text = props.title,
          style = props.style
        ))(),
        
        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Center,
          pos = (3, 2),
          width = width - 6,
          text = props.message,
          style = props.style
        ))(),
        
        <.button(
          ^.rbMouse := true,
          ^.rbWidth := 4,
          ^.rbHeight := 1,
          ^.rbTop := height - 3,
          ^.rbLeft := "center",
          ^.rbStyle := props.style,
          ^.rbOnPress := props.onClose,
          ^.content := " OK "
        )()
      )
    )
  }
}
