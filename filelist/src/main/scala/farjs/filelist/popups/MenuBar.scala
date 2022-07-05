package farjs.filelist.popups

import farjs.ui.popup.{Popup, PopupProps}
import farjs.ui.theme.Theme
import farjs.ui.{ButtonsPanel, ButtonsPanelProps}
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._

import scala.scalajs.js

case class MenuBarProps(onClose: () => Unit)

object MenuBar extends FunctionComponent[MenuBarProps] {

  private[popups] var popupComp: UiComponent[PopupProps] = Popup
  private[popups] var buttonsPanel: UiComponent[ButtonsPanelProps] = ButtonsPanel

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.wrapped
    val theme = Theme.current.popup.menu

    <(popupComp())(^.wrapped := PopupProps(
      onClose = props.onClose,
      onKeypress = { keyFull =>
        var processed = true
        keyFull match {
          case "f10" => props.onClose()
          case "down" =>
            process.stdin.emit("keypress", js.undefined, js.Dynamic.literal(
              name = "enter",
              ctrl = false,
              meta = false,
              shift = false
            ))
          case "up" =>
          case _ => processed = false
        }
        processed
      }
    ))(
      <.box(
        ^.rbHeight := 1,
        ^.rbStyle := theme
      )(
        <.box(
          ^.rbWidth := 49,
          ^.rbHeight := 1,
          ^.rbLeft := 2
        )(
          <(buttonsPanel())(^.wrapped := ButtonsPanelProps(
            top = 0,
            actions = List(
              "Left" -> {() => ()},
              "Files" -> {() => ()},
              "Commands" -> {() => ()},
              "Options" -> {() => ()},
              "Right" -> {() => ()}
            ),
            style = theme,
            padding = 2
          ))()
        )
      )
    )
  }
}
