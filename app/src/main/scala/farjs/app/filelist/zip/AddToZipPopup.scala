package farjs.app.filelist.zip

import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

case class AddToZipPopupProps(zipName: String,
                              action: AddToZipAction,
                              onAction: String => Unit,
                              onCancel: () => Unit)

object AddToZipPopup extends FunctionComponent[AddToZipPopupProps] {

  private[zip] var modalComp: UiComponent[ModalProps] = Modal
  private[zip] var textLineComp: UiComponent[TextLineProps] = TextLine
  private[zip] var textBoxComp: UiComponent[TextBoxProps] = TextBox
  private[zip] var horizontalLineComp: UiComponent[HorizontalLineProps] = HorizontalLine
  private[zip] var buttonsPanelComp: UiComponent[ButtonsPanelProps] = ButtonsPanel

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (zipName, setZipName) = useState(props.zipName)
    val size@(width, _) = (75, 8)
    val contentWidth = width - (paddingHorizontal + 2) * 2
    val contentLeft = 2
    val theme = Theme.current.popup.regular

    val onAction: js.Function0[Unit] = { () =>
      if (zipName.nonEmpty) {
        props.onAction(zipName)
      }
    }
    
    val actions = js.Array(
      ButtonsPanelAction(s"[ ${props.action} ]", onAction),
      ButtonsPanelAction("[ Cancel ]", props.onCancel)
    )

    <(modalComp())(^.wrapped := ModalProps(s"${props.action} files to archive", size, theme, props.onCancel))(
      <(textLineComp())(^.plain := TextLineProps(
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
      
      <(horizontalLineComp())(^.plain := HorizontalLineProps(
        left = 0,
        top = 3,
        length = width - paddingHorizontal * 2,
        lineCh = SingleBorder.horizontalCh,
        style = theme,
        startCh = DoubleChars.leftSingle,
        endCh = DoubleChars.rightSingle
      ))(),
      <(buttonsPanelComp())(^.plain := ButtonsPanelProps(
        top = 4,
        actions = actions,
        style = theme,
        margin = 2
      ))()
    )
  }
}
