package farjs.fs.popups

import farjs.filelist.history.{HistoryKind, HistoryProvider}
import farjs.fs.FSFoldersHistory
import farjs.ui.popup._
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object FoldersHistoryPopup extends FunctionComponent[FoldersHistoryPopupProps] {

  private[popups] var listPopup: ReactClass = ListPopup

  private val foldersHistoryKind = FSFoldersHistory.asInstanceOf[js.Dynamic].foldersHistoryKind.asInstanceOf[HistoryKind]
  
  protected def render(compProps: Props): ReactElement = {
    val historyProvider = HistoryProvider.useHistoryProvider()
    val (maybeItems, setItems) = useState(Option.empty[js.Array[String]])
    val props = compProps.plain

    useLayoutEffect({ () =>
      for {
        foldersHistory <- historyProvider.get(foldersHistoryKind).toFuture
        items <- foldersHistory.getAll().toFuture
      } yield {
        setItems(Some(items.map(_.item)))
      }
      ()
    }, Nil)

    maybeItems.map { items =>
      <(listPopup)(^.plain := ListPopupProps(
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
