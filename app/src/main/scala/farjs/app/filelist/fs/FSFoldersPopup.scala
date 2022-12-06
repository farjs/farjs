package farjs.app.filelist.fs

import farjs.filelist.FileListServices
import farjs.ui._
import farjs.ui.popup._
import farjs.ui.theme.Theme
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global

case class FSFoldersPopupProps(onChangeDir: String => Unit,
                               onClose: () => Unit)

object FSFoldersPopup extends FunctionComponent[FSFoldersPopupProps] {

  private[fs] var popupComp: UiComponent[PopupProps] = Popup
  private[fs] var modalContentComp: UiComponent[ModalContentProps] = ModalContent
  private[fs] var withSizeComp: UiComponent[WithSizeProps] = WithSize
  private[fs] var listBoxComp: UiComponent[ListBoxProps] = ListBox
  
  protected def render(compProps: Props): ReactElement = {
    val services = FileListServices.useServices
    val (maybeItems, setItems) = useState(Option.empty[List[String]])
    val props = compProps.wrapped
    val theme = Theme.current.popup.menu
    val textPaddingLeft = 2
    val textPaddingRight = 1
    val textPadding = textPaddingLeft + textPaddingRight
    val textPaddingLeftStr = " " * textPaddingLeft
    val textPaddingRightStr = " " * textPaddingRight

    useLayoutEffect({ () =>
      services.foldersHistory.getAll.map { items =>
        setItems(Some(items.toList))
      }
      ()
    }, Nil)

    maybeItems.map { items =>
      <(popupComp())(^.wrapped := PopupProps(onClose = props.onClose))(
        <(withSizeComp())(^.plain := WithSizeProps { (width, height) =>
          val maxContentWidth = {
            if (items.isEmpty) 2 * (paddingHorizontal + 1)
            else items.maxBy(_.length).length + 2 * (paddingHorizontal + 1)
          }
          val maxContentHeight = items.size + 2 * (paddingVertical + 1)

          val modalWidth = math.min(math.max(minWidth, maxContentWidth + textPadding), math.max(minWidth, width))
          val modalHeight = math.min(math.max(minHeight, maxContentHeight), math.max(minHeight, height - 4))

          val contentWidth = modalWidth - 2 * (paddingHorizontal + 1) // padding + border
          val contentHeight = modalHeight - 2 * (paddingVertical + 1)

          <(modalContentComp())(^.wrapped := ModalContentProps(
            title = "Folders history",
            size = (modalWidth, modalHeight),
            style = theme,
            padding = padding
          ))(
            <(listBoxComp())(^.wrapped := ListBoxProps(
              left = 1,
              top = 1,
              width = contentWidth,
              height = contentHeight,
              selected =
                if (items.isEmpty) 0
                else items.length - 1,
              items = items.map { item =>
                textPaddingLeftStr + TextLine.wrapText(item, contentWidth - textPadding) + textPaddingRightStr
              },
              style = theme,
              onAction = { index =>
                if (items.nonEmpty) {
                  props.onChangeDir(items(index))
                }
              }
            ))()
          )
        })()
      )
    }.orNull
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
