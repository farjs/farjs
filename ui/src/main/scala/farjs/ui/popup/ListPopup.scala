package farjs.ui.popup

import farjs.ui._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._

import scala.math.{max, min}

object ListPopup extends FunctionComponent[ListPopupProps] {

  private[popup] var popupComp: ReactClass = Popup
  private[popup] var modalContentComp: ReactClass = ModalContent
  private[popup] var withSizeComp: ReactClass = WithSize
  private[popup] var listBoxComp: ReactClass = ListBox
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val items = props.items
    val theme = Theme.useTheme().popup.menu
    val textPaddingLeft = props.textPaddingLeft.getOrElse(2)
    val textPaddingRight = props.textPaddingRight.getOrElse(1)
    val itemWrapPrefixLen = props.itemWrapPrefixLen.getOrElse(3)
    val textPaddingLen = textPaddingLeft + textPaddingRight
    val textPaddingLeftStr = " " * textPaddingLeft
    val textPaddingRightStr = " " * textPaddingRight

    <(popupComp)(^.plain := PopupProps(
      onClose = props.onClose,
      onKeypress = props.onKeypress
    ))(
      <(withSizeComp)(^.plain := WithSizeProps { (width, height) =>
        val maxContentWidth = {
          if (items.isEmpty) 2 * (paddingHorizontal + 1)
          else items.maxBy(_.length).length + 2 * (paddingHorizontal + 1)
        }
        val maxContentHeight = items.length + 2 * (paddingVertical + 1)

        val modalWidth = min(max(minWidth, maxContentWidth + textPaddingLen), max(minWidth, width))
        val modalHeight = min(max(minHeight, maxContentHeight), max(minHeight, height - 4))

        val contentWidth = modalWidth - 2 * (paddingHorizontal + 1) // padding + border
        val contentHeight = modalHeight - 2 * (paddingVertical + 1)

        <(modalContentComp)(^.plain := ModalContentProps(
          title = props.title,
          width = modalWidth,
          height = modalHeight,
          style = theme,
          padding = padding,
          footer = props.footer
        ))(
          <(listBoxComp)(^.plain := ListBoxProps(
            left = 1,
            top = 1,
            width = contentWidth,
            height = contentHeight,
            selected = props.selected.getOrElse(0),
            items = items.map { item =>
              textPaddingLeftStr +
                TextLine.wrapText(item, contentWidth - textPaddingLen, itemWrapPrefixLen) +
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
