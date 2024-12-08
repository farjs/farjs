package farjs.fs.popups

import farjs.filelist.FileListState
import farjs.filelist.stack.{WithStacks, WithStacksProps}
import farjs.fs.FSServices
import farjs.ui.popup._
import scommons.react._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

case class FolderShortcutsPopupProps(onChangeDir: String => Unit,
                                     onClose: () => Unit)

object FolderShortcutsPopup extends FunctionComponent[FolderShortcutsPopupProps] {

  private[popups] var listPopup: ReactClass = ListPopup
  
  protected def render(compProps: Props): ReactElement = {
    val stacks = WithStacks.useStacks()
    val services = FSServices.useServices
    val (maybeItems, setItems) = useState(Option.empty[List[js.UndefOr[String]]])
    val (selected, setSelected) = useState(0)
    val props = compProps.wrapped

    val onAction: js.Function1[Int, Unit] = { index =>
      maybeItems.foreach { items =>
        items(index).foreach { dir =>
          props.onChangeDir(dir)
        }
      }
    }
    
    val onKeypress: js.Function1[String, Boolean] = { key =>
      var processed = true
      key match {
        case "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" =>
          onAction(key.toInt)
        case "-" =>
          services.folderShortcuts.delete(selected).toFuture.foreach { _ =>
            setItems(maybeItems.map(items => items.updated(selected, js.undefined)))
          }
        case "+" =>
          val stackItem = WithStacksProps.active(stacks).stack.peekLast[FileListState]()
          stackItem.state.foreach { state =>
            val dir = state.currDir.path
            services.folderShortcuts.save(selected, dir).toFuture.foreach { _ =>
              setItems(maybeItems.map(items => items.updated(selected, dir)))
            }
          }
        case _ =>
          processed = false
      }

      processed
    }

    useLayoutEffect({ () =>
      services.folderShortcuts.getAll().toFuture.map { shortcuts =>
        setItems(Some(shortcuts.toList))
      }
      ()
    }, Nil)

    maybeItems.map { items =>
      <(listPopup)(^.plain := ListPopupProps(
        title = "Folder shortcuts",
        items = js.Array(items.zipWithIndex.map { case (maybeItem, i) =>
          s"$i: ${maybeItem.getOrElse("<none>")}"
        }: _*),
        onAction = onAction,
        onClose = props.onClose,
        onSelect = setSelected,
        onKeypress = onKeypress,
        footer = "Edit: +, -"
      ))()
    }.orNull
  }
}
