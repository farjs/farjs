package farjs.archiver

import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

case class AddToArchPopupProps(zipName: String,
                               action: AddToArchAction,
                               onAction: String => Unit,
                               onCancel: () => Unit)

object AddToArchPopup extends FunctionComponent[AddToArchPopupProps] {

  private[archiver] var modalComp: UiComponent[ModalProps] = Modal
  private[archiver] var textLineComp: ReactClass = TextLine
  private[archiver] var textBoxComp: UiComponent[TextBoxProps] = TextBox
  private[archiver] var horizontalLineComp: ReactClass = HorizontalLine
  private[archiver] var buttonsPanelComp: ReactClass = ButtonsPanel

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (zipName, setZipName) = useState(props.zipName)
    val (width, height) = (75, 8)
    val contentWidth = width - (paddingHorizontal + 2) * 2
    val contentLeft = 2
    val theme = Theme.useTheme().popup.regular

    val onAction: js.Function0[Unit] = { () =>
      if (zipName.nonEmpty) {
        props.onAction(zipName)
      }
    }
    
    val actions = js.Array(
      ButtonsPanelAction(s"[ ${props.action} ]", onAction),
      ButtonsPanelAction("[ Cancel ]", props.onCancel)
    )

    <(modalComp())(^.plain := ModalProps(s"${props.action} files to archive", width, height, theme, props.onCancel))(
      <(textLineComp)(^.plain := TextLineProps(
        align = TextAlign.left,
        left = contentLeft,
        top = 1,
        width = contentWidth,
        text = s"${props.action} to zip archive:",
        style = theme,
        padding = 0
      ))(),
      <(textBoxComp())(^.plain := TextBoxProps(
        left = contentLeft,
        top = 2,
        width = contentWidth,
        value = zipName,
        onChange = { value =>
          setZipName(value)
        },
        onEnter = onAction
      ))(),
      
      <(horizontalLineComp)(^.plain := HorizontalLineProps(
        left = 0,
        top = 3,
        length = width - paddingHorizontal * 2,
        lineCh = SingleChars.horizontal,
        style = theme,
        startCh = DoubleChars.leftSingle,
        endCh = DoubleChars.rightSingle
      ))(),
      <(buttonsPanelComp)(^.plain := ButtonsPanelProps(
        top = 4,
        actions = actions,
        style = theme,
        margin = 2
      ))()
    )
  }
}
