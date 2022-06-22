package farjs.filelist.sort

import farjs.filelist.stack.PanelStack
import farjs.ui.menu.{MenuPopup, MenuPopupProps}
import scommons.react._

import scala.scalajs.js

case class SortModesPopupProps(onClose: () => Unit)

object SortModesPopup extends FunctionComponent[SortModesPopupProps] {

  private[filelist] var menuPopup: UiComponent[MenuPopupProps] = MenuPopup

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val stackProps = PanelStack.usePanelStack
    val showOnLeft = !stackProps.isRight

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

  private[filelist] val items = List(
    "+ Name                 Ctrl-F3  ",
    "  Extension            Ctrl-F4  ",
    "  Modification time    Ctrl-F5  ",
    "  Size                 Ctrl-F6  ",
    "  Unsorted             Ctrl-F7  ",
    "  Creation time        Ctrl-F8  ",
    "  Access time          Ctrl-F9  "
  )
}
