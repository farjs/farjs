package farjs.filelist.popups

import farjs.filelist.FileListUiData
import farjs.filelist.stack.WithPanelStacks
import farjs.ui.menu._
import scommons.nodejs._
import scommons.react._

import scala.scalajs.js

object MenuController extends FunctionComponent[FileListUiData] {

  private[popups] var menuBarComp: UiComponent[MenuBarProps] = MenuBar

  protected def render(compProps: Props): ReactElement = {
    val stacks = WithPanelStacks.usePanelStacks
    val props = compProps.wrapped

    def onAction(menuIndex: Int, subIndex: Int): Unit = {
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
      <(menuBarComp())(^.wrapped := MenuBarProps(
        items = items,
        onAction = onAction,
        onClose = props.onClose
      ))()
    }
    else null
  }

  private lazy val actions = List[(String, List[(String, Option[Boolean], String)])](
    "Left" -> List(
      ("  Quick view    Ctrl-Q    ", Some(true), "C-q"),
      (SubMenu.separator, None, ""),
      ("  Sort modes    Ctrl-F12  ", Some(false), "C-f12"),
      ("  Re-read       Ctrl-R    ", Some(false), "C-r"),
      ("  Change drive  Alt-F1    ", Some(false), "M-l")
    ),
    "Files" -> List(
      ("  View            F3        ", None, "f3"),
      ("  Copy            F5        ", None, "f5"),
      ("  Rename or move  F6        ", None, "f6"),
      ("  Make folder     F7        ", None, "f7"),
      ("  Delete          F8        ", None, "f8"),
      (SubMenu.separator, None, ""),
      ("  Add to archive  Shift-F7  ", None, "S-f7"),
      (SubMenu.separator, None, ""),
      ("  Select group    +         ", None, "+"),
      ("  Unselect group  -         ", None, "-")
    ),
    "Commands" -> List(
      ("  Folders history   Alt-F12  ", None, "M-h"),
      (SubMenu.separator, None, ""),
      ("  Swap panels       Ctrl-U   ", None, "C-u"),
      ("  Quick search      Ctrl-S   ", None, "C-s"),
      (SubMenu.separator, None, ""),
      ("  Folder shortcuts  Ctrl-D   ", None, "C-d")
    ),
    "Options" -> List(
      ("  DevTools    F12  ", None, "f12")
    ),
    "Right" -> List(
      ("  Quick view    Ctrl-Q    ", Some(false), "C-q"),
      (SubMenu.separator, None, ""),
      ("  Sort modes    Ctrl-F12  ", Some(true), "C-f12"),
      ("  Re-read       Ctrl-R    ", Some(true), "C-r"),
      ("  Change drive  Alt-F2    ", Some(true), "M-r")
    )
  )

  private[popups] lazy val items: List[(String, List[String])] =
    actions.map { case (item, subItems) =>
      (item, subItems.map(_._1))
    }
}
