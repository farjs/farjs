package farjs.fs.popups

import farjs.filelist.FileListServices
import farjs.ui.popup._
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

case class FoldersHistoryPopupProps(onChangeDir: String => Unit,
                                    onClose: () => Unit)

object FoldersHistoryPopup extends FunctionComponent[FoldersHistoryPopupProps] {

  private[popups] var listPopup: UiComponent[ListPopupProps] = ListPopup
  
  protected def render(compProps: Props): ReactElement = {
    val services = FileListServices.useServices
    val (maybeItems, setItems) = useState(Option.empty[js.Array[String]])
    val props = compProps.wrapped

    useLayoutEffect({ () =>
      services.foldersHistory.getAll.map { items =>
        setItems(Some(js.Array(items: _*)))
      }
      ()
    }, Nil)

    maybeItems.map { items =>
      <(listPopup())(^.plain := ListPopupProps(
        title = "Folders history",
        items = items,
        onAction = { index =>
          props.onChangeDir(items(index))
        },
        onClose = props.onClose,
        selected = math.max(items.length - 1, 0)
      ))()
    }.orNull
  }
}
