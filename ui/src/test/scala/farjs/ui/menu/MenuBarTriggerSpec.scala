package farjs.ui.menu

import farjs.ui.popup.PopupOverlay
import scommons.nodejs._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class MenuBarTriggerSpec extends TestSpec with TestRendererUtils {

  it should "emit keypress event when onClick" in {
    //given
    val onKey = mockFunction[String, Boolean, Boolean, Boolean, Unit]
    val listener: js.Function2[js.Object, KeyboardKey, Unit] = { (_, key) =>
      onKey(
        key.name,
        key.ctrl.getOrElse(false),
        key.meta.getOrElse(false),
        key.shift.getOrElse(false)
      )
    }
    process.stdin.on("keypress", listener)

    val clickable = createTestRenderer(<(MenuBarTrigger())()()).root.children.head

    //then
    inSequence {
      onKey.expects("f9", false, false, false)
    }

    //when
    clickable.props.onClick(null)

    //cleanup
    process.stdin.removeListener("keypress", listener)
  }

  it should "render component" in {
    //when
    val result = createTestRenderer(<(MenuBarTrigger())()()).root

    //then
    assertComponents(result.children, List(
      <.box(
        ^.rbHeight := 1,
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := PopupOverlay.style
      )()
    ))
  }
}
