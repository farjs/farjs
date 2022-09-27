package farjs.app.filelist.fs

import farjs.ui._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._

case class FSFoldersPopupProps(selected: Int,
                               items: List[String],
                               onAction: String => Unit,
                               onClose: () => Unit)

object FSFoldersPopup extends FunctionComponent[FSFoldersPopupProps] {

  private[fs] var popupComp: UiComponent[PopupProps] = Popup
  private[fs] var modalContentComp: UiComponent[ModalContentProps] = ModalContent
  private[fs] var withSizeComp: UiComponent[WithSizeProps] = WithSize
  private[fs] var fsFoldersViewComp: UiComponent[FSFoldersViewProps] = FSFoldersView
  
  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val theme = Theme.current.popup.menu

    <(popupComp())(^.wrapped := PopupProps(onClose = props.onClose))(
      <(withSizeComp())(^.plain := WithSizeProps { (width, height) =>
        val maxContentWidth = {
          if (props.items.isEmpty) 2 * (paddingHorizontal + 1)
          else props.items.maxBy(_.length).length + 2 * (paddingHorizontal + 1)
        }
        val maxContentHeight = props.items.size + 2 * (paddingVertical + 1)

        val modalWidth = math.min(math.max(minWidth, maxContentWidth + 2), math.max(minWidth, width))
        val modalHeight = math.min(math.max(minHeight, maxContentHeight), math.max(minHeight, height - 4))

        val contentWidth = modalWidth - 2 * (paddingHorizontal + 1) // padding + border
        val contentHeight = modalHeight - 2 * (paddingVertical + 1)

        <(modalContentComp())(^.wrapped := ModalContentProps(
          title = "Folders history",
          size = (modalWidth, modalHeight),
          style = theme,
          padding = padding
        ))(
          <(fsFoldersViewComp())(^.wrapped := FSFoldersViewProps(
            left = 1,
            top = 1,
            width = contentWidth,
            height = contentHeight,
            selected = props.selected,
            items = props.items,
            style = theme,
            onAction = props.onAction
          ))()
        )
      })()
    )
  }

  private[fs] val paddingHorizontal = 2
  private[fs] val paddingVertical = 1

  private val minWidth = 50 + 2 * (paddingHorizontal + 1) // padding + border
  private val minHeight = 10 + 2 * (paddingVertical + 1)

  private[fs] val padding: BlessedPadding = new BlessedPadding {
    val left: Int = paddingHorizontal
    val right: Int = paddingHorizontal
    val top: Int = paddingVertical
    val bottom: Int = paddingVertical
  }
}
