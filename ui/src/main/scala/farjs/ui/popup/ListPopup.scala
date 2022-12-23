package farjs.ui.popup

import farjs.ui._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._

import scala.math.{max, min}

case class ListPopupProps(title: String,
                          items: List[String],
                          onAction: Int => Unit,
                          onClose: () => Unit,
                          onSelect: Int => Unit = _ => (),
                          onKeypress: String => Boolean = _ => false,
                          footer: Option[String] = None,
                          focusLast: Boolean = false,
                          textPaddingLeft: Int = 2,
                          textPaddingRight: Int = 1)

object ListPopup extends FunctionComponent[ListPopupProps] {

  private[popup] var popupComp: UiComponent[PopupProps] = Popup
  private[popup] var modalContentComp: UiComponent[ModalContentProps] = ModalContent
  private[popup] var withSizeComp: UiComponent[WithSizeProps] = WithSize
  private[popup] var listBoxComp: UiComponent[ListBoxProps] = ListBox
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val items = props.items
    val theme = Theme.current.popup.menu
    val textPadding = props.textPaddingLeft + props.textPaddingRight
    val textPaddingLeftStr = " " * props.textPaddingLeft
    val textPaddingRightStr = " " * props.textPaddingRight

    <(popupComp())(^.wrapped := PopupProps(onClose = props.onClose, onKeypress = props.onKeypress))(
      <(withSizeComp())(^.plain := WithSizeProps { (width, height) =>
        val maxContentWidth = {
          if (items.isEmpty) 2 * (paddingHorizontal + 1)
          else items.maxBy(_.length).length + 2 * (paddingHorizontal + 1)
        }
        val maxContentHeight = items.size + 2 * (paddingVertical + 1)

        val modalWidth = min(max(minWidth, maxContentWidth + textPadding), max(minWidth, width))
        val modalHeight = min(max(minHeight, maxContentHeight), max(minHeight, height - 4))

        val contentWidth = modalWidth - 2 * (paddingHorizontal + 1) // padding + border
        val contentHeight = modalHeight - 2 * (paddingVertical + 1)

        <(modalContentComp())(^.wrapped := ModalContentProps(
          title = props.title,
          size = (modalWidth, modalHeight),
          style = theme,
          padding = padding,
          footer = props.footer
        ))(
          <(listBoxComp())(^.wrapped := ListBoxProps(
            left = 1,
            top = 1,
            width = contentWidth,
            height = contentHeight,
            selected =
              if (items.isEmpty || !props.focusLast) 0
              else items.length - 1,
            items = items.map { item =>
              textPaddingLeftStr +
                TextLine.wrapText(item, contentWidth - textPadding) +
                textPaddingRightStr
            },
            style = theme,
            onAction = { index =>
              if (items.nonEmpty) {
                props.onAction(index)
              }
            },
            onSelect = props.onSelect
          ))()
        )
      })()
    )
  }

  private[popup] val paddingHorizontal = 2
  private[popup] val paddingVertical = 1

  private val minWidth = 50 + 2 * (paddingHorizontal + 1) // padding + border
  private val minHeight = 10 + 2 * (paddingVertical + 1)

  private[popup] val padding: BlessedPadding = new BlessedPadding {
    val left: Int = paddingHorizontal
    val right: Int = paddingHorizontal
    val top: Int = paddingVertical
    val bottom: Int = paddingVertical
  }
}
