package farjs.viewer

import farjs.filelist._
import farjs.ui.Dispatch
import farjs.ui.menu.{BottomMenu, BottomMenuProps}
import farjs.ui.popup.{Popup, PopupProps}
import farjs.viewer.ViewerPluginUi._
import scommons.react._
import scommons.react.blessed._
import scommons.react.hooks._

class ViewerPluginUi(dispatch: Dispatch, filePath: String, size: Double)
  extends FunctionComponent[FileListPluginUiProps] {

  protected def render(compProps: Props): ReactElement = {
    val inputRef = useRef[BlessedElement](null)
    val (viewport, setViewport) = useState(Option.empty[ViewerFileViewport])
    val props = compProps.plain

    def onKeypress(keyFull: String): Boolean = {
      var processed = true
      keyFull match {
        case "f3" | "f10" => props.onClose()
        case _ => processed = false
      }
      processed
    }

    val headerProps = viewport match {
      case None =>
        ViewerHeaderProps(filePath)
      case Some(vp) =>
        ViewerHeaderProps(
          filePath = filePath,
          encoding = vp.encoding,
          size = vp.size,
          column = vp.column,
          percent = vp.progress
        )
    }
    val menuItems = viewport match {
      case None => defaultMenuItems
      case Some(vp) =>
        var items = defaultMenuItems
        if (vp.wrap) {
          items = items.updated(1, "Unwrap")
        }
        items
    }

    <(popupComp())(^.wrapped := PopupProps(onClose = props.onClose, onKeypress = onKeypress))(
      <.box(
        ^.rbClickable := true,
        ^.rbAutoFocus := false
      )(
        <(viewerHeader())(^.wrapped := headerProps)(),
  
        <.button(
          ^.reactRef := inputRef,
          ^.rbTop := 1,
          ^.rbWidth := "100%",
          ^.rbHeight := "100%-2"
        )(
          <(viewerController())(^.wrapped := ViewerControllerProps(
            inputRef = inputRef,
            dispatch = dispatch,
            filePath = filePath,
            size = size,
            viewport = viewport,
            setViewport = setViewport
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

  private[viewer] val defaultMenuItems = List(
    /*  F1 */ "",
    /*  F2 */ "Wrap",
    /*  F3 */ "Quit",
    /*  F4 */ "",
    /*  F5 */ "",
    /*  F6 */ "",
    /*  F7 */ "",
    /*  F8 */ "Encodings",
    /*  F9 */ "",
    /* F10 */ "Quit",
    /* F11 */ "",
    /* F12 */ "DevTools"
  )
}
