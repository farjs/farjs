package farjs.filelist.copy

import farjs.filelist.api.FileListItem
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.hooks._

case class CopyItemsPopupProps(move: Boolean,
                               path: String,
                               items: Seq[FileListItem],
                               onAction: String => Unit,
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
        props.onAction(path)
      }
    }
    
    val title = if (props.move) "Rename/Move" else "Copy"
    val text = if (props.move) "Rename or move" else "Copy"
    val action = if (props.move) "Rename" else "Copy"
    val actions = List(
      s"[ $action ]" -> onCopy,
      "[ Cancel ]" -> props.onCancel
    )

    <(modalComp())(^.wrapped := ModalProps(title, size, theme, props.onCancel))(
      <(textLineComp())(^.wrapped := TextLineProps(
        align = TextLine.Left,
        pos = (contentLeft, 1),
        width = contentWidth,
        text = s"$text $itemsText to:",
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
