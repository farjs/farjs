package farjs.copymove

import farjs.filelist.FileListServices
import farjs.filelist.api.FileListItem
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

case class CopyItemsPopupProps(move: Boolean,
                               path: String,
                               items: Seq[FileListItem],
                               onAction: String => Unit,
                               onCancel: () => Unit)

object CopyItemsPopup extends FunctionComponent[CopyItemsPopupProps] {

  private[copymove] var modalComp: UiComponent[ModalProps] = Modal
  private[copymove] var textLineComp: ReactClass = TextLine
  private[copymove] var comboBoxComp: UiComponent[ComboBoxProps] = ComboBox
  private[copymove] var horizontalLineComp: ReactClass = HorizontalLine
  private[copymove] var buttonsPanelComp: ReactClass = ButtonsPanel

  protected def render(compProps: Props): ReactElement = {
    val services = FileListServices.useServices
    val (maybeItems, setItems) = useState(Option.empty[js.Array[String]])
    val props = compProps.wrapped
    val (path, setPath) = useState(props.path)
    val size@(width, _) = (75, 8)
    val contentWidth = width - (paddingHorizontal + 2) * 2
    val contentLeft = 2
    val theme = Theme.useTheme().popup.regular
    
    val count = props.items.size
    val itemsText =
      if (count > 1) s"$count items"
      else s"${props.items.headOption.map(i => s""""${i.name}"""").getOrElse("")}"

    val onCopy: js.Function0[Unit] = { () =>
      if (path.nonEmpty) {
        props.onAction(path)
      }
    }
    
    val title = if (props.move) "Rename/Move" else "Copy"
    val text = if (props.move) "Rename or move" else "Copy"
    val action = if (props.move) "Rename" else "Copy"
    val actions = js.Array(
      ButtonsPanelAction(s"[ $action ]", onCopy),
      ButtonsPanelAction("[ Cancel ]", props.onCancel)
    )

    useLayoutEffect({ () =>
      services.copyItemsHistory.getAll.map { items =>
        val itemsReversed = items.reverse
        setItems(Some(js.Array(itemsReversed: _*)))
      }
      ()
    }, Nil)

    maybeItems.map { items =>
      <(modalComp())(^.wrapped := ModalProps(title, size, theme, props.onCancel))(
        <(textLineComp)(^.plain := TextLineProps(
          align = TextAlign.left,
          left = contentLeft,
          top = 1,
          width = contentWidth,
          text = s"$text $itemsText to:",
          style = theme,
          padding = 0
        ))(),
        <(comboBoxComp())(^.plain := ComboBoxProps(
          left = contentLeft,
          top = 2,
          width = contentWidth,
          items = items,
          value = path,
          onChange = { value =>
            setPath(value)
          },
          onEnter = onCopy
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
    }.orNull
  }
}
