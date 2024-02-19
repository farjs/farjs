package farjs.file.popups

import farjs.file.{FileServices, FileViewHistory}
import farjs.ui.popup._
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

case class FileViewHistoryPopupProps(onAction: FileViewHistory => Unit,
                                     onClose: () => Unit)

object FileViewHistoryPopup extends FunctionComponent[FileViewHistoryPopupProps] {

  private[popups] var listPopup: UiComponent[ListPopupProps] = ListPopup
  
  protected def render(compProps: Props): ReactElement = {
    val services = FileServices.useServices
    val (maybeItems, setItems) = useState(Option.empty[js.Array[FileViewHistory]])
    val props = compProps.wrapped

    useLayoutEffect({ () =>
      services.fileViewHistory.getAll.map { items =>
        setItems(Some(js.Array(items: _*)))
      }
      ()
    }, Nil)

    maybeItems.map { items =>
      <(listPopup())(^.plain := ListPopupProps(
        title = "File view history",
        items = items.map { item =>
          val prefix =
            if (item.isEdit) "Edit: "
            else "View: "
          s"$prefix${item.path}"
        },
        onAction = { index =>
          props.onAction(items(index))
        },
        onClose = props.onClose,
        selected = math.max(items.length - 1, 0),
        itemWrapPrefixLen = 9
      ))()
    }.orNull
  }
}
