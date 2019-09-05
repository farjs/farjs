package scommons.farc.ui.popup

import scommons.farc.ui._
import scommons.farc.ui.border._
import scommons.react._
import scommons.react.blessed._

case class OkPopupProps(message: String,
                        onClose: () => Unit = () => ())

object OkPopup extends FunctionComponent[OkPopupProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (width, height) = (50, 6)
    val style = Popup.Styles.normal

    <(Popup())(^.wrapped := PopupProps(onClose = props.onClose))(
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
        <(DoubleBorder())(^.wrapped := DoubleBorderProps((width - 6, height - 2), style, (3, 1)))(),
        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Center,
          pos = (3, 1),
          width = width - 6,
          text = "Title",
          style = style
        ))(),
        
        <(TextLine())(^.wrapped := TextLineProps(
          align = TextLine.Center,
          pos = (3, 2),
          width = width - 6,
          text = props.message,
          style = style
        ))(),
        
        <.button(
          ^.rbMouse := true,
          ^.rbWidth := 4,
          ^.rbHeight := 1,
          ^.rbTop := height - 3,
          ^.rbLeft := "center",
          ^.rbStyle := style,
          ^.rbOnPress := { () =>
            println("OnPress")
          },
          ^.content := " OK "
        )()
      )
    )
  }
}
