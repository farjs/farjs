package farjs.filelist.popups

import farjs.filelist.FileListUiData
import farjs.filelist.stack.WithPanelStacks
import farjs.ui.menu._
import scommons.nodejs._
import scommons.react._

import scala.scalajs.js

object MenuController extends FunctionComponent[FileListUiData] {

  private[popups] var menuBarComp: ReactClass = MenuBar

  protected def render(compProps: Props): ReactElement = {
    val stacks = WithPanelStacks.usePanelStacks
    val props = compProps.wrapped

    val onAction: js.Function2[Int, Int, Unit] = { (menuIndex, subIndex) =>
      props.onClose()

      val action = actions(menuIndex)._2(subIndex)
      val keyFull = action._3
      val data = js.Dynamic.literal(
        name = keyFull.stripPrefix("C-").stripPrefix("M-").stripPrefix("S-"),
        full = keyFull,
        ctrl = keyFull.startsWith("C-"),
        meta = keyFull.startsWith("M-"),
        shift = keyFull.startsWith("S-")
      )
      action._2 match {
        case None => process.stdin.emit("keypress", js.undefined, data)
        case Some(false) => stacks.leftInput.emit("keypress", js.undefined, data)
        case Some(true) => stacks.rightInput.emit("keypress", js.undefined, data)
      }
    }
    
    if (props.showMenuPopup) {
      <(menuBarComp)(^.plain := MenuBarProps(
        items = items,
        onAction = onAction,
        onClose = props.onClose
      ))()
    }
    else null
  }

  private lazy val actions = js.Array[(String, js.Array[(String, Option[Boolean], String)])](
    "Left" -> js.Array(
      ("  Quick view    Ctrl-Q    ", Some(true), "C-q"),
      (SubMenu.separator, None, ""),
      ("  Sort modes    Ctrl-F12  ", Some(false), "C-f12"),
      ("  Re-read       Ctrl-R    ", Some(false), "C-r"),
      ("  Change drive  Alt-L     ", Some(false), "M-l")
    ),
    "Files" -> js.Array(
      ("  View            F3        ", None, "f3"),
      ("  Copy            F5        ", None, "f5"),
      ("  Rename or move  F6        ", None, "f6"),
      ("  Make folder     F7        ", None, "f7"),
      ("  Delete          F8        ", None, "f8"),
      (SubMenu.separator, None, ""),
      ("  Add to archive  Shift-F7  ", None, "S-f7"),
      (SubMenu.separator, None, ""),
      ("  Select group    Alt-S     ", None, "M-s"),
      ("  Unselect group  Alt-D     ", None, "M-d")
    ),
    "Commands" -> js.Array(
      ("  File view history  Alt-V   ", None, "M-v"),
      ("  Folders history    Alt-H   ", None, "M-h"),
      (SubMenu.separator, None, ""),
      ("  Swap panels        Ctrl-U  ", None, "C-u"),
      ("  Quick search       Ctrl-S  ", None, "C-s"),
      (SubMenu.separator, None, ""),
      ("  Folder shortcuts   Ctrl-D  ", None, "C-d")
    ),
    "Options" -> js.Array(
      ("  DevTools    F12  ", None, "f12")
    ),
    "Right" -> js.Array(
      ("  Quick view    Ctrl-Q    ", Some(false), "C-q"),
      (SubMenu.separator, None, ""),
      ("  Sort modes    Ctrl-F12  ", Some(true), "C-f12"),
      ("  Re-read       Ctrl-R    ", Some(true), "C-r"),
      ("  Change drive  Alt-R     ", Some(true), "M-r")
    )
  )

  private[popups] lazy val items: js.Array[MenuBarItem] =
    actions.map { case (item, subItems) =>
      MenuBarItem(item, subItems.map(_._1))
    }
}
