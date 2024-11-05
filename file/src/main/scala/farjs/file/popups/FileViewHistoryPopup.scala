package farjs.file.popups

import farjs.file.FileViewHistory
import farjs.file.FileViewHistory.fileViewsHistoryKind
import farjs.filelist.history.HistoryProvider
import farjs.ui.popup._
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

case class FileViewHistoryPopupProps(onAction: FileViewHistory => Unit,
                                     onClose: () => Unit)

object FileViewHistoryPopup extends FunctionComponent[FileViewHistoryPopupProps] {

  private[popups] var listPopup: ReactClass = ListPopup
  
  protected def render(compProps: Props): ReactElement = {
    val historyProvider = HistoryProvider.useHistoryProvider
    val (maybeItems, setItems) = useState(Option.empty[js.Array[FileViewHistory]])
    val props = compProps.wrapped

    useLayoutEffect({ () =>
      for {
        fileViewsHistory <- historyProvider.get(fileViewsHistoryKind).toFuture
        items <- fileViewsHistory.getAll.toFuture
      } yield {
        setItems(Some(items.flatMap(FileViewHistory.fromHistory)))
      }
      ()
    }, Nil)

    maybeItems.map { items =>
      <(listPopup)(^.plain := ListPopupProps(
        title = "File view history",
        items = items.map { item =>
          val prefix =
            if (item.params.isEdit) "Edit: "
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
