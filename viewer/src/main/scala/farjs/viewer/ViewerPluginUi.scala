package farjs.viewer

import farjs.filelist._
import farjs.ui.menu.{BottomMenu, BottomMenuProps}
import farjs.ui.popup.{Popup, PopupProps}
import farjs.ui.theme.Theme
import farjs.viewer.ViewerPluginUi._
import scommons.react._
import scommons.react.blessed._

class ViewerPluginUi(filePath: String)
  extends FunctionComponent[FileListPluginUiProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain

    def onKeypress(keyFull: String): Boolean = {
      var processed = true
      keyFull match {
        case "f3" | "f10" => props.onClose()
        case _ => processed = false
      }
      processed
    }

    <(popupComp())(^.wrapped := PopupProps(onClose = props.onClose, onKeypress = onKeypress))(
      <.box(
        ^.rbClickable := true,
        ^.rbAutoFocus := false
      )(
        <.text(
          ^.rbWidth := "100%",
          ^.rbHeight := 1,
          ^.rbStyle := headerStyle,
          ^.content := filePath
        )(),
  
        <.button(
          ^.rbTop := 1,
          ^.rbWidth := "100%",
          ^.rbHeight := "100%-2",
          ^.rbStyle := contentStyle,
          ^.content := ""
        )(),
  
        <.box(^.rbTop := "100%-1")(
          <(bottomMenuComp())(^.wrapped := BottomMenuProps(menuItems))()
        )
      )
    )
  }
}

object ViewerPluginUi {

  private[viewer] var popupComp: UiComponent[PopupProps] = Popup
  private[viewer] var bottomMenuComp: UiComponent[BottomMenuProps] = BottomMenu

  private[viewer] val menuItems = List(
    /*  F1 */ "",
    /*  F2 */ "",
    /*  F3 */ "Quit",
    /*  F4 */ "",
    /*  F5 */ "",
    /*  F6 */ "",
    /*  F7 */ "",
    /*  F8 */ "",
    /*  F9 */ "",
    /* F10 */ "Quit",
    /* F11 */ "",
    /* F12 */ "DevTools"
  )

  private[viewer] lazy val headerStyle: BlessedStyle = {
    val style = Theme.current.menu.item
    new BlessedStyle {
      override val bold = style.bold
      override val bg = style.bg
      override val fg = style.fg
    }
  }

  private[viewer] lazy val contentStyle: BlessedStyle = {
    val style = Theme.current.fileList.regularItem
    new BlessedStyle {
      override val bold = style.bold
      override val bg = style.bg
      override val fg = style.fg
    }
  }
}
