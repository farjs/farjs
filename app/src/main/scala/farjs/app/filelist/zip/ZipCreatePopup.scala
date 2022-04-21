package farjs.app.filelist.zip

import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.hooks._

case class ZipCreatePopupProps(zipName: String,
                               onAdd: String => Unit,
                               onCancel: () => Unit)

object ZipCreatePopup extends FunctionComponent[ZipCreatePopupProps] {

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

    val onAdd = { () =>
      if (zipName.nonEmpty) {
        props.onAdd(zipName)
      }
    }
    
    val actions = List(
      "[ Add ]" -> onAdd,
      "[ Cancel ]" -> props.onCancel
    )

    <(modalComp())(^.wrapped := ModalProps("Add files to archive", size, theme, props.onCancel))(
      <(textLineComp())(^.wrapped := TextLineProps(
        align = TextLine.Left,
        pos = (contentLeft, 1),
        width = contentWidth,
        text = "Add to zip archive:",
        style = theme,
        padding = 0
      ))(),
      <(textBoxComp())(^.wrapped := TextBoxProps(
        pos = (contentLeft, 2),
        width = contentWidth,
        value = zipName,
        onChange = { value =>
          setZipName(value)
        },
        onEnter = onAdd
      ))(),
      
      <(horizontalLineComp())(^.wrapped := HorizontalLineProps(
        pos = (0, 3),
        length = width - paddingHorizontal * 2,
        lineCh = SingleBorder.horizontalCh,
        style = theme,
        startCh = Some(DoubleBorder.leftSingleCh),
        endCh = Some(DoubleBorder.rightSingleCh)
      ))(),
      <(buttonsPanelComp())(^.wrapped := ButtonsPanelProps(
        top = 4,
        actions = actions,
        style = theme,
        margin = 2
      ))()
    )
  }
}
