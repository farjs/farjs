package farjs.filelist.sort

import farjs.filelist.stack.PanelStackComp
import farjs.ui.menu.{MenuPopup, MenuPopupProps}
import scommons.react._

import scala.scalajs.js

case class SortModesPopupProps(sort: FileListSort,
                               onClose: () => Unit)

object SortModesPopup extends FunctionComponent[SortModesPopupProps] {

  private[sort] var menuPopup: ReactClass = MenuPopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val stackProps = PanelStackComp.usePanelStack
    val showOnLeft = !stackProps.isRight

    val items = itemsAndModes.map { case (item, mode) =>
      if (mode == props.sort.mode) item.updated(0, if (props.sort.asc) '+' else '-')
      else item
    }

    val onSelect: js.Function1[Int, Unit] = { index =>
      props.onClose()

      val key = index + 3
      stackProps.panelInput.emit("keypress", js.undefined, js.Dynamic.literal(
        name = s"f$key+ctrl",
        full = s"C-f$key",
        ctrl = true
      ))
    }

    <(menuPopup)(^.plain := MenuPopupProps(
      title = "Sort by",
      items = js.Array(items: _*),
      getLeft = { width =>
        MenuPopup.getLeftPos(stackProps.width, showOnLeft, width)
      },
      onSelect = onSelect,
      onClose = props.onClose
    ))()
  }

  private val itemsAndModes = List(
    ("  Name                 Ctrl-F3  ", SortMode.Name),
    ("  Extension            Ctrl-F4  ", SortMode.Extension),
    ("  Modification time    Ctrl-F5  ", SortMode.ModificationTime),
    ("  Size                 Ctrl-F6  ", SortMode.Size),
    ("  Unsorted             Ctrl-F7  ", SortMode.Unsorted),
    ("  Creation time        Ctrl-F8  ", SortMode.CreationTime),
    ("  Access time          Ctrl-F9  ", SortMode.AccessTime)
  )
}
