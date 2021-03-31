package farjs.filelist.popups

import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

case class MakeFolderPopupProps(folderName: String,
                                multiple: Boolean,
                                onOk: (String, Boolean) => Unit,
                                onCancel: () => Unit)

object MakeFolderPopup extends FunctionComponent[MakeFolderPopupProps] {

  private[popups] var popupComp: UiComponent[PopupProps] = Popup
  private[popups] var doubleBorderComp: UiComponent[DoubleBorderProps] = DoubleBorder
  private[popups] var textLineComp: UiComponent[TextLineProps] = TextLine
  private[popups] var textBoxComp: UiComponent[TextBoxProps] = TextBox
  private[popups] var horizontalLineComp: UiComponent[HorizontalLineProps] = HorizontalLine
  private[popups] var checkBoxComp: UiComponent[CheckBoxProps] = CheckBox
  private[popups] var buttonsPanelComp: UiComponent[ButtonsPanelProps] = ButtonsPanel

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (folderName, setFolderName) = useState(props.folderName)
    val (multiple, setMultiple) = useState(props.multiple)
    val (width, height) = (75, 10)
    val theme = Theme.current.popup.regular

    val onOk = { () =>
      if (folderName.nonEmpty) {
        props.onOk(folderName, multiple)
      }
    }
    
    val actions = List(
      "[ OK ]" -> onOk,
      "[ Cancel ]" -> props.onCancel
    )

    <(popupComp())(^.wrapped := PopupProps(onClose = props.onCancel))(
      <.box(
        ^.rbClickable := true,
        ^.rbAutoFocus := false,
        ^.rbWidth := width,
        ^.rbHeight := height,
        ^.rbTop := "center",
        ^.rbLeft := "center",
        ^.rbShadow := true,
        ^.rbStyle := theme
      )(
        <(doubleBorderComp())(^.wrapped := DoubleBorderProps(
          size = (width - 6, height - 2),
          style = theme,
          pos = (3, 1),
          title = Some("Make Folder")
        ))(),
        
        <(textLineComp())(^.wrapped := TextLineProps(
          align = TextLine.Left,
          pos = (4, 2),
          width = width - 8,
          text = "Create the folder",
          style = theme
        ))(),
        <(textBoxComp())(^.wrapped := TextBoxProps(
          pos = (5, 3),
          width = width - 10,
          value = folderName,
          onChange = { value =>
            setFolderName(value)
          },
          onEnter = onOk
        ))(),
        <(horizontalLineComp())(^.wrapped := HorizontalLineProps(
          pos = (3, 4),
          length = width - 6,
          lineCh = SingleBorder.horizontalCh,
          style = theme,
          startCh = Some(DoubleBorder.leftSingleCh),
          endCh = Some(DoubleBorder.rightSingleCh)
        ))(),

        <(checkBoxComp())(^.wrapped := CheckBoxProps(
          pos = (5, 5),
          value = multiple,
          label = "Process multiple names",
          style = theme,
          onChange = { () =>
            setMultiple(!multiple)
          }
        ))(),
        <(horizontalLineComp())(^.wrapped := HorizontalLineProps(
          pos = (3, 6),
          length = width - 6,
          lineCh = SingleBorder.horizontalCh,
          style = theme,
          startCh = Some(DoubleBorder.leftSingleCh),
          endCh = Some(DoubleBorder.rightSingleCh)
        ))(),

        <(buttonsPanelComp())(^.wrapped := ButtonsPanelProps(
          top = height - 3,
          actions = actions,
          style = theme,
          margin = 2
        ))()
      )
    )
  }
}
