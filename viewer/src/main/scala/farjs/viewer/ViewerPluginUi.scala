package farjs.viewer

import farjs.filelist._
import farjs.ui.menu.{BottomMenu, BottomMenuProps}
import farjs.ui.popup.{Popup, PopupProps}
import farjs.viewer.ViewerPluginUi._
import scommons.react._
import scommons.react.blessed._
import scommons.react.redux.Dispatch

class ViewerPluginUi(dispatch: Dispatch, filePath: String, size: Double)
  extends FunctionComponent[FileListPluginUiProps] {

  protected def render(compProps: Props): ReactElement = {
    val props = compProps.plain
    val encoding = "utf-8"

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
        <(viewerHeader())(^.wrapped := ViewerHeaderProps(
          filePath = filePath,
          encoding = encoding,
          size = size,
          percent = 100
        ))(),
  
        <.button(
          ^.rbTop := 1,
          ^.rbWidth := "100%",
          ^.rbHeight := "100%-2"
        )(
          <(viewerController())(^.wrapped := ViewerControllerProps(
            dispatch = dispatch,
            filePath = filePath,
            encoding = encoding
          ))()
        ),
  
        <.box(^.rbTop := "100%-1")(
          <(bottomMenuComp())(^.wrapped := BottomMenuProps(menuItems))()
        )
      )
    )
  }
}

object ViewerPluginUi {

  private[viewer] var popupComp: UiComponent[PopupProps] = Popup
  private[viewer] var viewerHeader: UiComponent[ViewerHeaderProps] = ViewerHeader
  private[viewer] var viewerController: UiComponent[ViewerControllerProps] = ViewerController
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
}
