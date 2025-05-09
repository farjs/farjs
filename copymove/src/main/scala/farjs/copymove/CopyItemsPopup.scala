package farjs.copymove

import farjs.copymove.CopyMoveUi.copyItemsHistoryKind
import farjs.filelist.history.HistoryProvider
import farjs.ui._
import farjs.ui.border._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object CopyItemsPopup extends FunctionComponent[CopyItemsPopupProps] {

  private[copymove] var modalComp: ReactClass = Modal
  private[copymove] var textLineComp: ReactClass = TextLine
  private[copymove] var comboBoxComp: ReactClass = ComboBox
  private[copymove] var horizontalLineComp: ReactClass = HorizontalLine
  private[copymove] var buttonsPanelComp: ReactClass = ButtonsPanel

  protected def render(compProps: Props): ReactElement = {
    val historyProvider = HistoryProvider.useHistoryProvider()
    val (maybeItems, setItems) = useState(Option.empty[js.Array[String]])
    val props = compProps.plain
    val (path, setPath) = useState(props.path)
    val (width, height) = (75, 8)
    val contentWidth = width - (paddingHorizontal + 2) * 2
    val contentLeft = 2
    val theme = Theme.useTheme().popup.regular
    
    val count = props.items.length
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
      for {
        copyItemsHistory <- historyProvider.get(copyItemsHistoryKind).toFuture
        items <- copyItemsHistory.getAll().toFuture
      } yield {
        val itemsReversed = items.reverse.map(_.item)
        setItems(Some(itemsReversed))
      }
      ()
    }, Nil)

    maybeItems.map { items =>
      <(modalComp)(^.plain := ModalProps(title, width, height, theme, props.onCancel))(
        <(textLineComp)(^.plain := TextLineProps(
          align = TextAlign.left,
          left = contentLeft,
          top = 1,
          width = contentWidth,
          text = s"$text $itemsText to:",
          style = theme,
          padding = 0
        ))(),
        <(comboBoxComp)(^.plain := ComboBoxProps(
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
