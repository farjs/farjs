package farjs.file.popups

import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.hooks._

import scala.scalajs.js

object TextSearchPopup extends FunctionComponent[TextSearchPopupProps] {

  private[popups] var modalComp: ReactClass = Modal
  private[popups] var textLineComp: ReactClass = TextLine
  private[popups] var comboBoxComp: ReactClass = ComboBox
  private[popups] var horizontalLineComp: ReactClass = HorizontalLine
  private[popups] var buttonsPanelComp: ReactClass = ButtonsPanel

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val (searchText, setSearchText) = useState("")
    val (width, height) = (75, 8)
    val contentWidth = width - (paddingHorizontal + 2) * 2
    val contentLeft = 2
    val theme = Theme.useTheme().popup.regular

    val onSearch: js.Function0[Unit] = { () =>
      if (searchText.nonEmpty) {
        props.onSearch(searchText)
      }
    }
    
    val actions = js.Array(
      ButtonsPanelAction("[ Search ]", onSearch),
      ButtonsPanelAction("[ Cancel ]", props.onCancel)
    )

    <(modalComp)(^.plain := ModalProps("Search", width, height, theme, props.onCancel))(
      <(textLineComp)(^.plain := TextLineProps(
        align = TextAlign.left,
        left = contentLeft,
        top = 1,
        width = contentWidth,
        text = "Search for",
        style = theme,
        padding = 0
      ))(),
      <(comboBoxComp)(^.plain := ComboBoxProps(
        left = contentLeft,
        top = 2,
        width = contentWidth,
        items = js.Array(),
        value = searchText,
        onChange = { value =>
          setSearchText(value)
        },
        onEnter = onSearch
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
