package farjs.ui.popup

import farjs.ui._
import farjs.ui.border._
import scommons.react._
import scommons.react.blessed._

case class MessageBoxProps(title: String,
                           message: String,
                           actions: List[MessageBoxAction],
                           style: BlessedStyle)

object MessageBox extends FunctionComponent[MessageBoxProps] {

  private[popup] var popupComp: UiComponent[PopupProps] = Popup
  private[popup] var doubleBorderComp: UiComponent[DoubleBorderProps] = DoubleBorder
  private[popup] var textLineComp: UiComponent[TextLineProps] = TextLine
  private[popup] var buttonsPanelComp: UiComponent[ButtonsPanelProps] = ButtonsPanel

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val width = 60
    val textWidth = width - 8
    val textLines = UI.splitText(props.message, textWidth - 2) //exclude padding
    val height = 5 + textLines.size
    val onClose = props.actions.find(_.triggeredOnClose).map(_.onAction)

    val actions = props.actions.map { action =>
      (action.label, action.onAction)
    }

    <(popupComp())(^.wrapped := PopupProps(
      onClose = onClose.getOrElse(() => ()),
      closable = onClose.isDefined
    ))(
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
        <(doubleBorderComp())(^.wrapped := DoubleBorderProps(
          size = (width - 6, height - 2),
          style = props.style,
          pos = (3, 1),
          title = Some(props.title)
        ))(),
        
        textLines.zipWithIndex.map { case (text, index) =>
          <(textLineComp())(^.key := s"$index", ^.wrapped := TextLineProps(
            align = TextLine.Center,
            pos = (4, 2 + index),
            width = textWidth,
            text = text,
            style = props.style
          ))()
        },
        
        <(buttonsPanelComp())(^.wrapped := ButtonsPanelProps(
          top = height - 3,
          actions = actions,
          style = props.style,
          padding = 1
        ))()
      )
    )
  }
}
