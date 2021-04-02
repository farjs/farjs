package farjs.filelist.copy

import farjs.filelist.api.FileListItem
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.hooks._

case class CopyItemsPopupProps(path: String,
                               items: Seq[FileListItem],
                               onCopy: String => Unit,
                               onCancel: () => Unit)

object CopyItemsPopup extends FunctionComponent[CopyItemsPopupProps] {

  private[copy] var modalComp: UiComponent[ModalProps] = Modal
  private[copy] var textLineComp: UiComponent[TextLineProps] = TextLine
  private[copy] var textBoxComp: UiComponent[TextBoxProps] = TextBox
  private[copy] var horizontalLineComp: UiComponent[HorizontalLineProps] = HorizontalLine
  private[copy] var buttonsPanelComp: UiComponent[ButtonsPanelProps] = ButtonsPanel

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val (path, setPath) = useState(props.path)
    val size@(width, _) = (75, 8)
    val contentWidth = width - (paddingHorizontal + 2) * 2
    val contentLeft = 2
    val theme = Theme.current.popup.regular
    
    val count = props.items.size
    val itemsText =
      if (count > 1) s"$count items"
      else s"${props.items.headOption.map(i => s""""${i.name}"""").getOrElse("")}"

    val onCopy = { () =>
      if (path.nonEmpty) {
        props.onCopy(path)
      }
    }
    
    val actions = List(
      "[ Copy ]" -> onCopy,
      "[ Cancel ]" -> props.onCancel
    )

    <(modalComp())(^.wrapped := ModalProps("Copy", size, theme, props.onCancel))(
      <(textLineComp())(^.wrapped := TextLineProps(
        align = TextLine.Left,
        pos = (contentLeft, 1),
        width = contentWidth,
        text = s"Copy $itemsText to:",
        style = theme,
        padding = 0
      ))(),
      <(textBoxComp())(^.wrapped := TextBoxProps(
        pos = (contentLeft, 2),
        width = contentWidth,
        value = path,
        onChange = { value =>
          setPath(value)
        },
        onEnter = onCopy
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
