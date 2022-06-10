package farjs.filelist.popups

import farjs.filelist.popups.FileListPopupsActions._
import farjs.ui._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.hooks._

case class SelectPopupProps(pattern: String,
                            action: FileListPopupSelect,
                            onAction: String => Unit,
                            onCancel: () => Unit)

object SelectPopup extends FunctionComponent[SelectPopupProps] {

  private[popups] var modalComp: UiComponent[ModalProps] = Modal
  private[popups] var textBoxComp: UiComponent[TextBoxProps] = TextBox

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (pattern, setPattern) = useState(props.pattern)
    val size@(width, _) = (55, 5)
    val contentWidth = width - (paddingHorizontal + 2) * 2
    val contentLeft = 2
    val theme = Theme.current.popup.regular

    val onAction = { () =>
      if (pattern.nonEmpty) {
        props.onAction(pattern)
      }
    }
    
    <(modalComp())(^.wrapped := ModalProps(
      title =
        if (props.action == ShowSelect) "Select"
        else "Deselect",
      size = size,
      style = theme,
      onCancel = props.onCancel
    ))(
      <(textBoxComp())(^.wrapped := TextBoxProps(
        pos = (contentLeft, 1),
        width = contentWidth,
        value = pattern,
        onChange = { value =>
          setPattern(value)
        },
        onEnter = onAction
      ))()
    )
  }
}
