package farjs.viewer

import farjs.filelist._
import farjs.ui.menu.{BottomMenu, BottomMenuProps}
import farjs.ui.popup.{Popup, PopupProps}
import farjs.viewer.ViewerPluginUi._
import scommons.react.blessed._
import scommons.react.hooks._
import scommons.react.{raw, _}

import scala.scalajs.js

class ViewerPluginUi(filePath: String, size: Double)
  extends FunctionComponent[FileListPluginUiProps] {

  protected def render(compProps: Props): ReactElement = {
    val inputRef = raw.React.useRef(null)
    val (viewport, setViewport) = useState(js.undefined: js.UndefOr[ViewerFileViewport])
    val props = compProps.plain

    val onKeypress: js.Function1[String, Boolean] = { keyFull =>
      var processed = true
      keyFull match {
        case "f3" | "f10" => props.onClose()
        case _ => processed = false
      }
      processed
    }

    val headerProps = viewport.toOption match {
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
    val menuItems = viewport.toOption match {
      case None => defaultMenuItems
      case Some(vp) =>
        var items = defaultMenuItems
        if (vp.wrap) {
          items = items.updated(1, "Unwrap")
        }
        items
    }

    <(popupComp)(^.plain := PopupProps(
      onClose = props.onClose,
      onKeypress = onKeypress
    ))(
      <.box(
        ^.rbClickable := true,
        ^.rbAutoFocus := false
      )(
        <(viewerHeader)(^.plain := headerProps)(),
  
        <.button(
          ^.ref := { el: BlessedElement =>
            inputRef.current = el
          },          ^.rbTop := 1,
          ^.rbWidth := "100%",
          ^.rbHeight := "100%-2"
        )(
          <(viewerController())(^.plain := ViewerControllerProps(
            inputRef = inputRef,
            dispatch = props.dispatch,
            filePath = filePath,
            size = size,
            viewport = viewport,
            setViewport = setViewport
          ))()
        ),
  
        <.box(^.rbTop := "100%-1")(
          <(bottomMenuComp)(^.plain := BottomMenuProps(menuItems))()
        )
      )
    )
  }
}

object ViewerPluginUi {

  private[viewer] var popupComp: ReactClass = Popup
  private[viewer] var viewerHeader: ReactClass = ViewerHeader
  private[viewer] var viewerController: UiComponent[ViewerControllerProps] = ViewerController
  private[viewer] var bottomMenuComp: ReactClass = BottomMenu

  private[viewer] val defaultMenuItems = js.Array(
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
