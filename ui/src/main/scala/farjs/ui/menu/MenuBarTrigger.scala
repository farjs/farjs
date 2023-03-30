package farjs.ui.menu

import farjs.ui.popup.PopupOverlay
import scommons.nodejs._
import scommons.react._
import scommons.react.blessed._

import scala.scalajs.js

object MenuBarTrigger extends FunctionComponent[Unit] {

  protected def render(compProps: Props): ReactElement = {
    <.box(
      ^.rbHeight := 1,
      ^.rbClickable := true,
      ^.rbMouse := true,
      ^.rbAutoFocus := false,
      ^.rbStyle := PopupOverlay.style,
      ^.rbOnClick := { _ =>
        process.stdin.emit("keypress", js.undefined, js.Dynamic.literal(
          name = "f9",
          ctrl = false,
          meta = false,
          shift = false
        ))
      }
    )()
  }
}
