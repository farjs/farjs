package farjs.filelist.popups

import farjs.filelist.history.HistoryProvider
import farjs.filelist.popups.SelectController.selectPatternsHistoryKind
import farjs.ui._
import farjs.ui.popup.ModalContent._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

case class SelectPopupProps(showSelect: Boolean,
                            onAction: String => Unit,
                            onCancel: () => Unit)

object SelectPopup extends FunctionComponent[SelectPopupProps] {

  private[popups] var modalComp: ReactClass = Modal
  private[popups] var comboBoxComp: ReactClass = ComboBox

  protected def render(compProps: Props): ReactElement = {
    val historyProvider = HistoryProvider.useHistoryProvider
    val (maybeItems, setItems) = useState(Option.empty[js.Array[String]])
    val props = compProps.wrapped
    val (pattern, setPattern) = useState("")
    val (width, height) = (55, 5)
    val contentWidth = width - (paddingHorizontal + 2) * 2
    val contentLeft = 2
    val theme = Theme.useTheme().popup.regular

    val onAction: js.Function0[Unit] = { () =>
      if (pattern.nonEmpty) {
        props.onAction(pattern)
      }
    }
    
    useLayoutEffect({ () =>
      for {
        selectPatternsHistory <- historyProvider.get(selectPatternsHistoryKind).toFuture
        items <- selectPatternsHistory.getAll.toFuture
      } yield {
        val itemsReversed = items.reverse.map(_.item)
        itemsReversed.headOption.foreach { last =>
          setPattern(last)
        }
        setItems(Some(itemsReversed))
      }
      ()
    }, Nil)

    maybeItems.map { items =>
      <(modalComp)(^.plain := ModalProps(
        title =
          if (props.showSelect) "Select"
          else "Deselect",
        width = width,
        height = height,
        style = theme,
        onCancel = props.onCancel
      ))(
        <(comboBoxComp)(^.plain := ComboBoxProps(
          left = contentLeft,
          top = 1,
          width = contentWidth,
          items = items,
          value = pattern,
          onChange = { value =>
            setPattern(value)
          },
          onEnter = onAction
        ))()
      )
    }.orNull
  }
}
