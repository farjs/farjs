package farjs.filelist.popups

import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

case class MakeFolderPopupProps(folderName: String,
                                multiple: Boolean,
                                onOk: (String, Boolean) => Unit,
                                onCancel: () => Unit)

object MakeFolderPopup extends FunctionComponent[MakeFolderPopupProps] {

  private[popups] var modalComp: UiComponent[ModalProps] = Modal
  private[popups] var textLineComp: UiComponent[TextLineProps] = TextLine
  private[popups] var textBoxComp: UiComponent[TextBoxProps] = TextBox
  private[popups] var horizontalLineComp: UiComponent[HorizontalLineProps] = HorizontalLine
  private[popups] var checkBoxComp: UiComponent[CheckBoxProps] = CheckBox
  private[popups] var buttonsPanelComp: UiComponent[ButtonsPanelProps] = ButtonsPanel

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (folderName, setFolderName) = useState(props.folderName)
    val (multiple, setMultiple) = useState(props.multiple)
    val size@(width, _) = (75, 10)
    val contentWidth = width - (paddingHorizontal + 2) * 2
    val contentLeft = 2
    val theme = Theme.current.popup.regular

    val onOk: js.Function0[Unit] = { () =>
      if (folderName.nonEmpty) {
        props.onOk(folderName, multiple)
      }
    }
    
    val actions = js.Array(
      ButtonsPanelAction("[ OK ]", onOk),
      ButtonsPanelAction("[ Cancel ]", props.onCancel)
    )

    <(modalComp())(^.wrapped := ModalProps("Make Folder", size, theme, props.onCancel))(
      <(textLineComp())(^.plain := TextLineProps(
        align = TextAlign.left,
        left = contentLeft,
        top = 1,
        width = contentWidth,
        text = "Create the folder",
        style = theme,
        padding = 0
      ))(),
      <(textBoxComp())(^.plain := TextBoxProps(
        left = contentLeft,
        top = 2,
        width = contentWidth,
        value = folderName,
        onChange = { value =>
          setFolderName(value)
        },
        onEnter = onOk
      ))(),
      
      <(horizontalLineComp())(^.wrapped := HorizontalLineProps(
        pos = (0, 3),
        length = width - paddingHorizontal * 2,
        lineCh = SingleBorder.horizontalCh,
        style = theme,
        startCh = Some(DoubleBorder.leftSingleCh),
        endCh = Some(DoubleBorder.rightSingleCh)
      ))(),
      <(checkBoxComp())(^.plain := CheckBoxProps(
        left = contentLeft,
        top = 4,
        value = multiple,
        label = "Process multiple names",
        style = theme,
        onChange = { () =>
          setMultiple(!multiple)
        }
      ))(),
      
      <(horizontalLineComp())(^.wrapped := HorizontalLineProps(
        pos = (0, 5),
        length = width - paddingHorizontal * 2,
        lineCh = SingleBorder.horizontalCh,
        style = theme,
        startCh = Some(DoubleBorder.leftSingleCh),
        endCh = Some(DoubleBorder.rightSingleCh)
      ))(),
      <(buttonsPanelComp())(^.plain := ButtonsPanelProps(
        top = 6,
        actions = actions,
        style = theme,
        margin = 2
      ))()
    )
  }
}
