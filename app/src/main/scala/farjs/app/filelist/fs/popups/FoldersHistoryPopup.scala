package farjs.app.filelist.fs.popups

import farjs.filelist.FileListServices
import farjs.ui.popup._
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global

case class FoldersHistoryPopupProps(onChangeDir: String => Unit,
                                    onClose: () => Unit)

object FoldersHistoryPopup extends FunctionComponent[FoldersHistoryPopupProps] {

  private[popups] var listPopup: UiComponent[ListPopupProps] = ListPopup
  
  protected def render(compProps: Props): ReactElement = {
    val services = FileListServices.useServices
    val (maybeItems, setItems) = useState(Option.empty[List[String]])
    val props = compProps.wrapped

    useLayoutEffect({ () =>
      services.foldersHistory.getAll.map { items =>
        setItems(Some(items.toList))
      }
      ()
    }, Nil)

    maybeItems.map { items =>
      <(listPopup())(^.wrapped := ListPopupProps(
        title = "Folders history",
        items = items,
        onAction = { index =>
          props.onChangeDir(items(index))
        },
        onClose = props.onClose,
        focusLast = true
      ))()
    }.orNull
  }
}
