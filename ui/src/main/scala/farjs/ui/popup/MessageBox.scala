package farjs.ui.popup

import farjs.ui._
import farjs.ui.popup.ModalContent._
import scommons.react._
import scommons.react.blessed._

case class MessageBoxProps(title: String,
                           message: String,
                           actions: List[MessageBoxAction],
                           style: BlessedStyle)

object MessageBox extends FunctionComponent[MessageBoxProps] {

  private[popup] var popupComp: UiComponent[PopupProps] = Popup
  private[popup] var modalContentComp: UiComponent[ModalContentProps] = ModalContent
  private[popup] var textLineComp: UiComponent[TextLineProps] = TextLine
  private[popup] var buttonsPanelComp: UiComponent[ButtonsPanelProps] = ButtonsPanel

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val width = 60
    val textWidth = width - (paddingHorizontal + 2) * 2
    val textLines = UI.splitText(props.message, textWidth)
    val height = (paddingVertical + 1) * 2 + textLines.size + 1
    val onClose = props.actions.find(_.triggeredOnClose).map(_.onAction)

    val actions = props.actions.map { action =>
      (action.label, action.onAction)
    }

    <(popupComp())(^.wrapped := PopupProps(
      onClose = onClose.getOrElse(() => ()),
      closable = onClose.isDefined
    ))(
      <(modalContentComp())(^.wrapped := ModalContentProps(
        title = props.title,
        size = (width, height),
        style = props.style
      ))(
        textLines.zipWithIndex.map { case (text, index) =>
          <(textLineComp())(^.key := s"$index", ^.wrapped := TextLineProps(
            align = TextLine.Center,
            pos = (2, 1 + index),
            width = textWidth,
            text = text,
            style = props.style,
            padding = 0
          ))()
        },
        
        <(buttonsPanelComp())(^.wrapped := ButtonsPanelProps(
          top = 1 + textLines.size,
          actions = actions,
          style = props.style,
          padding = 1
        ))()
      )
    )
  }
}
