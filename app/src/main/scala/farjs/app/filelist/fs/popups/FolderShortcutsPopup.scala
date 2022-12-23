package farjs.app.filelist.fs.popups

import farjs.filelist.stack.WithPanelStacks
import farjs.filelist.{FileListServices, FileListState}
import farjs.ui.popup._
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global

case class FolderShortcutsPopupProps(onChangeDir: String => Unit,
                                     onClose: () => Unit)

object FolderShortcutsPopup extends FunctionComponent[FolderShortcutsPopupProps] {

  private[popups] var listPopup: UiComponent[ListPopupProps] = ListPopup
  
  protected def render(compProps: Props): ReactElement = {
    val stacks = WithPanelStacks.usePanelStacks
    val services = FileListServices.useServices
    val (maybeItems, setItems) = useState(Option.empty[List[Option[String]]])
    val (selected, setSelected) = useState(0)
    val props = compProps.wrapped

    def onAction(index: Int): Unit = {
      maybeItems.foreach { items =>
        items(index).foreach { dir =>
          props.onChangeDir(dir)
        }
      }
    }
    
    def onKeypress(key: String): Boolean = {
      var processed = true
      key match {
        case "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" =>
          onAction(key.toInt)
        case "-" =>
          setItems(maybeItems.map(items => items.updated(selected, None)))
        case "+" =>
          val stackItem = stacks.activeStack.peekLast[FileListState]
          stackItem.state.foreach { state =>
            val dir = state.currDir.path
            setItems(maybeItems.map(items => items.updated(selected, Some(dir))))
          }
        case _ =>
          processed = false
      }

      processed
    }

    useLayoutEffect({ () =>
      //TODO: load shortcuts
      services.foldersHistory.getAll.map { items =>
        val initItems = List.fill(10)(Option.empty[String])
        val resItems = items.takeRight(10).zipWithIndex.foldLeft(initItems) {
          case (res, (dir, i)) => res.updated(i, Some(dir))
        }
        setItems(Some(resItems))
      }
      ()
    }, Nil)

    maybeItems.map { items =>
      <(listPopup())(^.wrapped := ListPopupProps(
        title = "Folder shortcuts",
        items = items.zipWithIndex.map { case (maybeItem, i) =>
          s"$i: ${maybeItem.getOrElse("<none>")}"
        },
        onAction = onAction,
        onClose = props.onClose,
        onSelect = setSelected,
        onKeypress = onKeypress,
        footer = Some("Edit: +, -")
      ))()
    }.orNull
  }
}
