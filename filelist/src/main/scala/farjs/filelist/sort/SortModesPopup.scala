package farjs.filelist.sort

import farjs.filelist.stack.PanelStack
import farjs.ui.menu.{MenuPopup, MenuPopupProps}
import scommons.react._

import scala.scalajs.js

case class SortModesPopupProps(mode: SortMode,
                               ascending: Boolean,
                               onClose: () => Unit)

object SortModesPopup extends FunctionComponent[SortModesPopupProps] {

  private[sort] var menuPopup: UiComponent[MenuPopupProps] = MenuPopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val stackProps = PanelStack.usePanelStack
    val showOnLeft = !stackProps.isRight

    val items = itemsAndModes.map { case (item, mode) =>
      if (mode == props.mode) item.updated(0, if (props.ascending) '+' else '-')
      else item
    }

    def onSelect(index: Int): Unit = {
      props.onClose()

      val key = index + 3
      stackProps.panelInput.emit("keypress", js.undefined, js.Dynamic.literal(
        name = s"f$key+ctrl",
        full = s"C-f$key",
        ctrl = true
      ))
    }

    <(menuPopup())(^.wrapped := MenuPopupProps(
      title = "Sort by",
      items = items,
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
