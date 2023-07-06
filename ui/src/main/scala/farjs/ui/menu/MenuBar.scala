package farjs.ui.menu

import farjs.ui.popup.{Popup, PopupProps}
import farjs.ui.theme.Theme
import farjs.ui.{ButtonsPanel, ButtonsPanelAction, ButtonsPanelProps}
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

case class MenuBarProps(items: List[(String, List[String])],
                        onAction: (Int, Int) => Unit,
                        onClose: () => Unit)

object MenuBar extends FunctionComponent[MenuBarProps] {

  private[menu] var popupComp: UiComponent[PopupProps] = Popup
  private[menu] var buttonsPanel: ReactClass = ButtonsPanel
  private[menu] var subMenuComp: UiComponent[SubMenuProps] = SubMenu

  protected def render(compProps: Props): ReactElement = {
    val (maybeSubMenu, setSubMenu) = useState[Option[(Int, Int)]](None)
    val props = compProps.wrapped
    val theme = Theme.useTheme().popup.menu
    val marginLeft = 2
    val padding = 2
    val width = props.items.foldLeft(0) { (res, item) =>
      res + item._1.length + padding * 2
    }
    val actions = props.items.zipWithIndex.map { case ((item, _), index) =>
      ButtonsPanelAction(item, () => setSubMenu(Some(index -> 0)))
    }

    def getLeftPos(menuIndex: Int): Int = {
      var leftPos = marginLeft
      for (i <- 0 until menuIndex) {
        leftPos += actions(i).label.length + padding * 2
      }
      leftPos
    }
    
    def onAction(menuIndex: Int, subIndex: Int): Unit = {
      Future { // call action on the next tick
        props.onAction(menuIndex, subIndex)
      }
    }
    
    val onKeypress: js.Function1[String, Boolean] = { keyFull =>
      var processed = true
      keyFull match {
        case "f10" => props.onClose()
        case "escape" =>
          if (maybeSubMenu.isDefined) setSubMenu(None)
          else processed = false
        case "down" =>
          maybeSubMenu match {
            case Some((menuIndex, subIndex)) =>
              val subItems = props.items(menuIndex)._2
              val newSubIndex =
                if (subIndex == subItems.size - 1) 0
                else if (subItems(subIndex + 1) == SubMenu.separator) subIndex + 2
                else subIndex + 1
              setSubMenu(Some((menuIndex, newSubIndex)))
            case None =>
              process.stdin.emit("keypress", js.undefined, js.Dynamic.literal(
                name = "enter",
                ctrl = false,
                meta = false,
                shift = false
              ))
          }
        case "up" =>
          maybeSubMenu.foreach { case (menuIndex, subIndex) =>
            val subItems = props.items(menuIndex)._2
            val newSubIndex =
              if (subIndex == 0) subItems.size - 1
              else if (subItems(subIndex - 1) == SubMenu.separator) subIndex - 2
              else subIndex - 1
            setSubMenu(Some((menuIndex, newSubIndex)))
          }
        case "tab" | "right" =>
          processed = false
          maybeSubMenu.foreach { case (menuIndex, _) =>
            val newIndex = if (menuIndex == actions.size - 1) 0 else menuIndex + 1
            setSubMenu(Some((newIndex, 0)))
          }
        case "S-tab" | "left" =>
          processed = false
          maybeSubMenu.foreach { case (menuIndex, _) =>
            val newIndex = if (menuIndex == 0) actions.size - 1 else menuIndex - 1
            setSubMenu(Some((newIndex, 0)))
          }
        case "enter" | "space" =>
          maybeSubMenu match {
            case None => processed = false
            case Some((menuIndex, subIndex)) => onAction(menuIndex, subIndex)
          }
        case _ => processed = false
      }
      processed
    }

    <.>()(
      <(popupComp())(^.plain := PopupProps(onClose = props.onClose: js.Function0[Unit], onKeypress = onKeypress))(
        <.box(
          ^.rbHeight := 1,
          ^.rbStyle := theme
        )(
          <.box(
            ^.rbWidth := width,
            ^.rbHeight := 1,
            ^.rbLeft := marginLeft
          )(
            <(buttonsPanel)(^.plain := ButtonsPanelProps(
              top = 0,
              actions = js.Array(actions: _*),
              style = theme,
              padding = padding
            ))()
          )
        )
      ),

      maybeSubMenu.map { case (menuIndex, subIndex) =>
        <(subMenuComp())(^.wrapped := SubMenuProps(
          selected = subIndex,
          items = props.items(menuIndex)._2,
          top = 1,
          left = getLeftPos(menuIndex),
          onClick = { index =>
            onAction(menuIndex, index)
          }
        ))()
      }
    )
  }
}
