package farjs.filelist.popups

import farjs.filelist.FileListServices
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

  private[popups] var modalComp: UiComponent[ModalProps] = Modal
  private[popups] var comboBoxComp: UiComponent[ComboBoxProps] = ComboBox

  protected def render(compProps: Props): ReactElement = {
    val services = FileListServices.useServices
    val (maybeItems, setItems) = useState(Option.empty[js.Array[String]])
    val props = compProps.wrapped
    val (pattern, setPattern) = useState("")
    val size@(width, _) = (55, 5)
    val contentWidth = width - (paddingHorizontal + 2) * 2
    val contentLeft = 2
    val theme = Theme.useTheme().popup.regular

    val onAction: js.Function0[Unit] = { () =>
      if (pattern.nonEmpty) {
        props.onAction(pattern)
      }
    }
    
    useLayoutEffect({ () =>
      services.selectPatternsHistory.getAll.map { items =>
        val itemsReversed = items.reverse
        itemsReversed.headOption.foreach { last =>
          setPattern(last)
        }
        setItems(Some(js.Array(itemsReversed: _*)))
      }
      ()
    }, Nil)

    maybeItems.map { items =>
      <(modalComp())(^.wrapped := ModalProps(
        title =
          if (props.showSelect) "Select"
          else "Deselect",
        size = size,
        style = theme,
        onCancel = props.onCancel
      ))(
        <(comboBoxComp())(^.plain := ComboBoxProps(
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
