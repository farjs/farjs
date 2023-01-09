package farjs.ui.popup

import org.scalatest._
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class PopupOverlaySpec extends TestSpec with TestRendererUtils {

  it should "call onOpen and focus first element if focusable when mount" in {
    //given
    val onOpen = mockFunction[Unit]
    val props = PopupProps(onClose = () => (), onOpen = onOpen)
    val onMock = mockFunction[String, js.Function, Unit]
    val focusFirstMock = mockFunction[Unit]
    val formMock = literal("on" -> onMock, "focusFirst" -> focusFirstMock)
    onMock.expects("element keypress", *)
    onMock.expects("element focus", *)
    
    var focused = false
    focusFirstMock.expects().onCall { () =>
      focused = true
    }
    onOpen.expects()

    //when
    createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock
      else null
    })

    //then
    focused shouldBe true
  }

  it should "call onOpen and not focus first element if not focusable when mount" in {
    //given
    val onOpen = mockFunction[Unit]
    val props = PopupProps(onClose = () => (), onOpen = onOpen, focusable = false)
    val onMock = mockFunction[String, js.Function, Unit]
    val focusFirstMock = mockFunction[Unit]
    val formMock = literal("on" -> onMock, "focusFirst" -> focusFirstMock)
    onMock.expects("element keypress", *)
    onMock.expects("element focus", *)
    
    //then
    focusFirstMock.expects().never()
    onOpen.expects()

    //when
    createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock
      else null
    })
  }

  it should "remove listeners when unmount" in {
    //given
    val props = PopupProps(onClose = () => (), focusable = false)
    val onMock = mockFunction[String, js.Function, Unit]
    val offMock = mockFunction[String, js.Function, Unit]
    val formMock = literal("on" -> onMock, "off" -> offMock)
    onMock.expects("element keypress", *)
    onMock.expects("element focus", *)
    
    val renderer = createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else null
    })

    //then
    offMock.expects("element keypress", *)
    offMock.expects("element focus", *)

    //when
    renderer.unmount()
  }

  it should "re-subscribe element listeners when update" in {
    //given
    val props = PopupProps(onClose = () => (), focusable = false)
    val onMock = mockFunction[String, js.Function, Unit]
    val offMock = mockFunction[String, js.Function, Unit]
    val formMock = literal("on" -> onMock, "off" -> offMock)
    onMock.expects("element keypress", *)
    onMock.expects("element focus", *)
    
    val renderer = createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else null
    })

    //then
    onMock.expects("element keypress", *)
    onMock.expects("element focus", *)
    offMock.expects("element keypress", *)
    offMock.expects("element focus", *)

    //when
    renderer.update(<(PopupOverlay())(^.wrapped := props.copy(onClose = () => ()))())
  }

  it should "listen to element keys and perform actions" in {
    //given
    val onClose = mockFunction[Unit]
    val onKeypress = mockFunction[String, Boolean]
    val props = PopupProps(onClose = onClose, focusable = false, onKeypress = onKeypress)
    val onMock = mockFunction[String, js.Function, Unit]
    val offMock = mockFunction[String, js.Function, Unit]
    val focusNextMock = mockFunction[Unit]
    val focusPreviousMock = mockFunction[Unit]
    val formMock = literal(
      "on" -> onMock,
      "off" -> offMock,
      "focusNext" -> focusNextMock,
      "focusPrevious" -> focusPreviousMock
    )

    var keyListener: js.Function3[BlessedElement, js.Object, KeyboardKey, Unit] = null
    onMock.expects("element keypress", *).onCall { (_, listener) =>
      keyListener = listener.asInstanceOf[js.Function3[BlessedElement, js.Object, KeyboardKey, Unit]]
    }
    onMock.expects("element focus", *)
    createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock
      else null
    })

    def check(defaultPrevented: Boolean, handled: Boolean, keys: String*): Unit = keys.foreach { key =>
      //then
      if (!defaultPrevented) {
        onKeypress.expects(key).returning(handled)
        if (!handled) {
          key match {
            case "escape" => onClose.expects()
            case "tab" | "down" | "right" => focusNextMock.expects()
            case "S-tab" | "up" | "left" => focusPreviousMock.expects()
            case _ =>
          }
        }
      }
      //when
      keyListener(null, null, literal(
        "full" -> key,
        "defaultPrevented" -> defaultPrevented
      ).asInstanceOf[KeyboardKey])
    }

    //when & then
    check(defaultPrevented = false, handled = true, "escape")
    check(defaultPrevented = false, handled = false, "escape")
    check(defaultPrevented = true, handled = false, "escape")
    check(defaultPrevented = false, handled = false, "tab", "down", "right")
    check(defaultPrevented = true, handled = false, "tab", "down", "right")
    check(defaultPrevented = false, handled = false, "S-tab", "up", "left")
    check(defaultPrevented = true, handled = false, "S-tab", "up", "left")
    check(defaultPrevented = false, handled = false, "unknown")
    check(defaultPrevented = true, handled = false, "unknown")
  }

  it should "not call onClose if non-closable when escape" in {
    //given
    val onClose = mockFunction[Unit]
    val props = PopupProps(onClose = onClose, focusable = false, closable = false)
    val onMock = mockFunction[String, js.Function, Unit]
    val formMock = literal("on" -> onMock)
    
    //then
    onClose.expects().never()

    onMock.expects("element keypress", *).onCall { (_, listener) =>
      //when
      val keyListener = listener.asInstanceOf[js.Function3[BlessedElement, js.Object, KeyboardKey, Unit]]
      keyListener(null, null, literal("full" -> "escape").asInstanceOf[KeyboardKey])
    }
    onMock.expects("element focus", *)

    //when
    createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock
      else null
    })
  }

  it should "set form._selected element when child element is focused" in {
    //given
    val props = PopupProps(onClose = () => (), focusable = false, closable = false)
    val childElement = literal("some" -> "childElement").asInstanceOf[BlessedElement]
    val onMock = mockFunction[String, js.Function, Unit]
    val formMock = literal("on" -> onMock, "_selected" -> childElement)
    
    //then
    onMock.expects("element keypress", *)
    onMock.expects("element focus", *).onCall { (_, listener) =>
      //when
      val focusListener = listener.asInstanceOf[js.Function1[BlessedElement, Unit]]
      focusListener(childElement)
    }

    //when
    createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock
      else null
    })
  }

  it should "call onClose if closable when onClick" in {
    //given
    val onClose = mockFunction[Unit]
    val props = PopupProps(onClose = onClose)
    val onMock = mockFunction[String, js.Function, Unit]
    val focusFirstMock = mockFunction[Unit]
    val formMock = literal("on" -> onMock, "focusFirst" -> focusFirstMock)

    focusFirstMock.expects()
    onMock.expects("element keypress", *)
    onMock.expects("element focus", *)

    val form = createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock
      else null
    }).root.children(0)

    //then
    onClose.expects()
    
    //when
    form.props.onClick()
  }
  
  it should "not call onClose if non-closable when onClick" in {
    //given
    val onClose = mockFunction[Unit]
    val props = PopupProps(onClose = onClose, closable = false)
    val onMock = mockFunction[String, js.Function, Unit]
    val focusFirstMock = mockFunction[Unit]
    val formMock = literal("on" -> onMock, "focusFirst" -> focusFirstMock)
    focusFirstMock.expects()
    onMock.expects("element keypress", *)
    onMock.expects("element focus", *)

    val form = createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock
      else null
    }).root.children(0)

    //then
    onClose.expects().never()
    
    //when
    form.props.onClick()
  }
  
  it should "render component" in {
    //given
    val children: ReactElement = <.box()("test popup child")
    val props = PopupProps(onClose = () => ())
    val onMock = mockFunction[String, js.Function, Unit]
    val focusFirstMock = mockFunction[Unit]
    val formMock = literal("on" -> onMock, "focusFirst" -> focusFirstMock)
    focusFirstMock.expects()
    onMock.expects("element keypress", *)
    onMock.expects("element focus", *)

    //when
    val result = createTestRenderer(<(PopupOverlay())(^.wrapped := props)(children), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock
      else null
    }).root.children(0)

    //then
    assertPopupOverlay(result, children)
  }

  private def assertPopupOverlay(result: TestInstance, children: ReactElement): Assertion = {
    assertNativeComponent(result,
      <.form(
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := PopupOverlay.style
      )(
        children
      )
    )
  }
}
