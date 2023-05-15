package farjs.text.popups

import farjs.text.{FileViewHistory, TextServices}
import farjs.ui.popup._
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global

case class FileViewHistoryPopupProps(onAction: FileViewHistory => Unit,
                                     onClose: () => Unit)

object FileViewHistoryPopup extends FunctionComponent[FileViewHistoryPopupProps] {

  private[popups] var listPopup: UiComponent[ListPopupProps] = ListPopup
  
  protected def render(compProps: Props): ReactElement = {
    val services = TextServices.useServices
    val (maybeItems, setItems) = useState(Option.empty[List[FileViewHistory]])
    val props = compProps.wrapped

    useLayoutEffect({ () =>
      services.fileViewHistory.getAll.map { items =>
        setItems(Some(items.toList))
      }
      ()
    }, Nil)

    maybeItems.map { items =>
      <(listPopup())(^.wrapped := ListPopupProps(
        title = "File view history",
        items = items.map(_.path),
        onAction = { index =>
          props.onAction(items(index))
        },
        onClose = props.onClose,
        selected = math.max(items.length - 1, 0)
      ))()
    }.orNull
  }
}
