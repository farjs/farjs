package farjs.ui.popup

import farjs.ui.popup.PopupOverlaySpec._
import org.scalatest._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

class PopupOverlaySpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  it should "call onOpen and focus first element if focusable when mount" in {
    //given
    val onOpen = mockFunction[Unit]
    val props = PopupProps(onClose = () => (), onOpen = onOpen)
    val formMock = mock[FormElementMock]

    (formMock.on _).expects("element keypress", *)
    (formMock.on _).expects("element focus", *)
    
    var focused = false
    (formMock.focusFirst _).expects().onCall { () =>
      focused = true
    }
    onOpen.expects()

    //when
    createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else null
    })

    //then
    eventually {
      focused shouldBe true
    }
  }

  it should "call onOpen and not focus first element if not focusable when mount" in {
    //given
    val onOpen = mockFunction[Unit]
    val props = PopupProps(onClose = () => (), onOpen = onOpen, focusable = false)
    val formMock = mock[FormElementMock]

    (formMock.on _).expects("element keypress", *)
    (formMock.on _).expects("element focus", *)
    
    //then
    (formMock.focusFirst _).expects().never()
    onOpen.expects()

    //when
    createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else null
    })
    
    Succeeded
  }

  it should "remove listeners when unmount" in {
    //given
    val props = PopupProps(onClose = () => (), focusable = false)
    val formMock = mock[FormElementMock]

    (formMock.on _).expects("element keypress", *)
    (formMock.on _).expects("element focus", *)
    
    val renderer = createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else null
    })

    //then
    (formMock.off _).expects("element keypress", *)
    (formMock.off _).expects("element focus", *)

    //when
    renderer.unmount()
    
    Succeeded
  }

  it should "re-subscribe element listeners when update" in {
    //given
    val props = PopupProps(onClose = () => (), focusable = false)
    val formMock = mock[FormElementMock]

    (formMock.on _).expects("element keypress", *)
    (formMock.on _).expects("element focus", *)
    
    val renderer = createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else null
    })

    //then
    (formMock.on _).expects("element keypress", *)
    (formMock.on _).expects("element focus", *)
    (formMock.off _).expects("element keypress", *)
    (formMock.off _).expects("element focus", *)

    //when
    renderer.update(<(PopupOverlay())(^.wrapped := props.copy(onClose = () => ()))())

    Succeeded
  }

  it should "listen to element keys and perform actions" in {
    //given
    val onClose = mockFunction[Unit]
    val props = PopupProps(onClose = onClose, focusable = false)
    val formMock = mock[FormElementMock]

    var keyListener: js.Function3[BlessedElement, js.Object, KeyboardKey, Unit] = null
    (formMock.on _).expects("element keypress", *).onCall { (_, listener) =>
      keyListener = listener.asInstanceOf[js.Function3[BlessedElement, js.Object, KeyboardKey, Unit]]
    }
    (formMock.on _).expects("element focus", *)
    createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else null
    })

    def check(defaultPrevented: Boolean, keys: String*): Unit = keys.foreach { key =>
      //then
      if (!defaultPrevented) key match {
        case "escape" => onClose.expects()
        case "tab" | "down" | "right" => (formMock.focusNext _).expects()
        case "S-tab" | "up" | "left" => (formMock.focusPrevious _).expects()
        case _ =>
      }
      //when
      keyListener(null, null, js.Dynamic.literal(
        "full" -> key,
        "defaultPrevented" -> defaultPrevented
      ).asInstanceOf[KeyboardKey])
    }

    //when & then
    check(defaultPrevented = false, "escape")
    check(defaultPrevented = true, "escape")
    check(defaultPrevented = false, "tab", "down", "right")
    check(defaultPrevented = true, "tab", "down", "right")
    check(defaultPrevented = false, "S-tab", "up", "left")
    check(defaultPrevented = true, "S-tab", "up", "left")
    check(defaultPrevented = false, "unknown")
    check(defaultPrevented = true, "unknown")

    Succeeded
  }

  it should "not call onClose if non-closable when escape" in {
    //given
    val onClose = mockFunction[Unit]
    val props = PopupProps(onClose = onClose, focusable = false, closable = false)
    val formMock = mock[FormElementMock]
    
    //then
    onClose.expects().never()

    (formMock.on _).expects("element keypress", *).onCall { (_, listener) =>
      //when
      val keyListener = listener.asInstanceOf[js.Function3[BlessedElement, js.Object, KeyboardKey, Unit]]
      keyListener(null, null, js.Dynamic.literal("full" -> "escape").asInstanceOf[KeyboardKey])
    }
    (formMock.on _).expects("element focus", *)

    //when
    createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else null
    })

    Succeeded
  }

  it should "set form._selected element when child element is focused" in {
    //given
    val props = PopupProps(onClose = () => (), focusable = false, closable = false)
    val formMock = mock[FormElementMock]
    val childElement = js.Dynamic.literal("some" -> "childElement").asInstanceOf[BlessedElement]
    
    //then
    (formMock._selected_= _).expects(childElement)
    
    (formMock.on _).expects("element keypress", *)
    (formMock.on _).expects("element focus", *).onCall { (_, listener) =>
      //when
      val focusListener = listener.asInstanceOf[js.Function1[BlessedElement, Unit]]
      focusListener(childElement)
    }

    //when
    createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else null
    })

    Succeeded
  }

  it should "call onClose if closable when onClick" in {
    //given
    val onClose = mockFunction[Unit]
    val props = PopupProps(onClose = onClose)
    val formMock = mock[FormElementMock]

    (formMock.focusFirst _).expects()
    (formMock.on _).expects("element keypress", *)
    (formMock.on _).expects("element focus", *)

    val form = createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else null
    }).root.children(0)

    //then
    onClose.expects()
    
    //when
    form.props.onClick()

    Succeeded
  }
  
  it should "not call onClose if non-closable when onClick" in {
    //given
    val onClose = mockFunction[Unit]
    val props = PopupProps(onClose = onClose, closable = false)
    val formMock = mock[FormElementMock]

    (formMock.focusFirst _).expects()
    (formMock.on _).expects("element keypress", *)
    (formMock.on _).expects("element focus", *)

    val form = createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else null
    }).root.children(0)

    //then
    onClose.expects().never()
    
    //when
    form.props.onClick()

    Succeeded
  }
  
  it should "render component" in {
    //given
    val children: ReactElement = <.box()("test popup child")
    val props = PopupProps(onClose = () => ())
    val formMock = mock[FormElementMock]

    (formMock.focusFirst _).expects()
    (formMock.on _).expects("element keypress", *)
    (formMock.on _).expects("element focus", *)

    //when
    val result = createTestRenderer(<(PopupOverlay())(^.wrapped := props)(children), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else null
    }).root.children(0)

    //then
    assertPopupOverlay(result, props, children)
  }

  private def assertPopupOverlay(result: TestInstance,
                                 props: PopupProps,
                                 children: ReactElement): Assertion = {
    
    assertNativeComponent(result,
      <.form(
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := PopupOverlay.overlayStyle
      )(
        children
      )
    )
  }
}

object PopupOverlaySpec {

  @JSExportAll
  trait FormElementMock {
    
    def _selected_=(el: BlessedElement): Unit
    def focusFirst(): Unit
    def focusNext(): Unit
    def focusPrevious(): Unit
    
    def on(event: String, listener: js.Function): Unit
    def off(event: String, listener: js.Function): Unit
  }
}
