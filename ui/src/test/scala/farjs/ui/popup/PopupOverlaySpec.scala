package farjs.ui.popup

import farjs.ui.popup.PopupOverlaySpec._
import scommons.react._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.{ShallowRendererUtils, TestRendererUtils}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

class PopupOverlaySpec extends TestSpec
  with ShallowRendererUtils
  with TestRendererUtils {

  it should "call onOpen and focus first element if focusable when mount" in {
    //given
    val onOpen = mockFunction[Unit]
    val props = PopupProps(onClose = () => (), onOpen = onOpen)
    val screenMock = mock[BlessedScreenMock]
    val formMock = mock[FormElementMock]

    (formMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.focused _).expects().returning(null)
    (formMock.on _).expects("element keypress", *)
    (formMock.on _).expects("element focus", *)
    
    //then
    (formMock.focusFirst _).expects()
    onOpen.expects()

    //when
    createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else fail(s"Need mock for: ${el.`type`}")
    })
  }

  it should "call onOpen and not focus first element if not focusable when mount" in {
    //given
    val onOpen = mockFunction[Unit]
    val props = PopupProps(onClose = () => (), onOpen = onOpen, focusable = false)
    val screenMock = mock[BlessedScreenMock]
    val formMock = mock[FormElementMock]

    (formMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.focused _).expects().returning(null)
    (formMock.on _).expects("element keypress", *)
    (formMock.on _).expects("element focus", *)
    
    //then
    (formMock.focusFirst _).expects().never()
    onOpen.expects()

    //when
    createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else fail(s"Need mock for: ${el.`type`}")
    })
  }

  it should "focus focused element and remove listeners when unmount" in {
    //given
    val props = PopupProps(onClose = () => (), focusable = false)
    val screenMock = mock[BlessedScreenMock]
    val formMock = mock[FormElementMock]
    val focused = mock[BlessedElementMock]
    var capturedKeyListener: js.Function3[BlessedElement, js.Object, KeyboardKey, Unit] = null
    var capturedFocusListener: js.Function1[BlessedElement, Unit] = null

    (formMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.focused _).expects().returning(focused.asInstanceOf[BlessedElement])
    (formMock.on _).expects("element keypress", *).onCall { (_, listener) =>
      capturedKeyListener = listener.asInstanceOf[js.Function3[BlessedElement, js.Object, KeyboardKey, Unit]]
    }
    (formMock.on _).expects("element focus", *).onCall { (_, listener) =>
      capturedFocusListener = listener.asInstanceOf[js.Function1[BlessedElement, Unit]]
    }
    
    val renderer = createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else fail(s"Need mock for: ${el.`type`}")
    })

    //then
    (focused.focus _).expects()
    (formMock.off _).expects("element keypress", capturedKeyListener)
    (formMock.off _).expects("element focus", capturedFocusListener)

    //when
    renderer.unmount()
  }

  it should "only remove listeners if no focused element when unmount" in {
    //given
    val props = PopupProps(onClose = () => (), focusable = false)
    val screenMock = mock[BlessedScreenMock]
    val formMock = mock[FormElementMock]

    (formMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.focused _).expects().returning(null)
    (formMock.on _).expects("element keypress", *)
    (formMock.on _).expects("element focus", *)
    
    val renderer = createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else fail(s"Need mock for: ${el.`type`}")
    })

    //then
    (formMock.off _).expects("element keypress", *)
    (formMock.off _).expects("element focus", *)

    //when
    renderer.unmount()
  }

  it should "re-subscribe element listeners when update" in {
    //given
    val props = PopupProps(onClose = () => (), focusable = false)
    val screenMock = mock[BlessedScreenMock]
    val formMock = mock[FormElementMock]

    (formMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.focused _).expects().returning(null)
    (formMock.on _).expects("element keypress", *)
    (formMock.on _).expects("element focus", *)
    
    val renderer = createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else fail(s"Need mock for: ${el.`type`}")
    })

    //then
    (formMock.on _).expects("element keypress", *)
    (formMock.on _).expects("element focus", *)
    (formMock.off _).expects("element keypress", *)
    (formMock.off _).expects("element focus", *)

    //when
    renderer.update(<(PopupOverlay())(^.wrapped := props.copy(onClose = () => ()))())
  }

  it should "listen to element keys and perform actions" in {
    //given
    val onClose = mockFunction[Unit]
    val props = PopupProps(onClose = onClose, focusable = false)
    val screenMock = mock[BlessedScreenMock]
    val formMock = mock[FormElementMock]

    (formMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.focused _).expects().returning(null)
    
    var keyListener: js.Function3[BlessedElement, js.Object, KeyboardKey, Unit] = null
    (formMock.on _).expects("element keypress", *).onCall { (_, listener) =>
      keyListener = listener.asInstanceOf[js.Function3[BlessedElement, js.Object, KeyboardKey, Unit]]
    }
    (formMock.on _).expects("element focus", *)
    createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else fail(s"Need mock for: ${el.`type`}")
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
  }

  it should "not call onClose if non-closable when escape" in {
    //given
    val onClose = mockFunction[Unit]
    val props = PopupProps(onClose = onClose, focusable = false, closable = false)
    val screenMock = mock[BlessedScreenMock]
    val formMock = mock[FormElementMock]

    (formMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.focused _).expects().returning(null)
    
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
      else fail(s"Need mock for: ${el.`type`}")
    })
  }

  it should "set form._selected element when child element is focused" in {
    //given
    val props = PopupProps(onClose = () => (), focusable = false, closable = false)
    val screenMock = mock[BlessedScreenMock]
    val formMock = mock[FormElementMock]
    val childElement = js.Dynamic.literal("some" -> "childElement").asInstanceOf[BlessedElement]

    (formMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.focused _).expects().returning(null)
    
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
      else fail(s"Need mock for: ${el.`type`}")
    })
  }

  it should "call onClose if closable when onClick" in {
    //given
    val onClose = mockFunction[Unit]
    val props = PopupProps(onClose = onClose)
    val comp = shallowRender(<(PopupOverlay())(^.wrapped := props)())

    //then
    onClose.expects()
    
    //when
    comp.props.onClick()
  }
  
  it should "not call onClose if non-closable when onClick" in {
    //given
    val onClose = mockFunction[Unit]
    val props = PopupProps(onClose = onClose, closable = false)
    val comp = shallowRender(<(PopupOverlay())(^.wrapped := props)())

    //then
    onClose.expects().never()
    
    //when
    comp.props.onClick()
  }
  
  it should "render component" in {
    //given
    val children: ReactElement = <.box()("test popup child")
    val props = PopupProps(onClose = () => ())

    //when
    val result = shallowRender(<(PopupOverlay())(^.wrapped := props)(children))

    //then
    assertPopupOverlay(result, props, children)
  }

  private def assertPopupOverlay(result: ShallowInstance,
                                 props: PopupProps,
                                 children: ReactElement): Unit = {
    
    assertNativeComponent(result,
      <.form(
        ^.rbClickable := true,
        ^.rbMouse := true,
        ^.rbAutoFocus := false,
        ^.rbStyle := PopupOverlay.overlayStyle
      )(), { resChildren: List[ShallowInstance] =>
        val List(child) = resChildren
        child shouldBe children
      }
    )
  }
}

object PopupOverlaySpec {

  @JSExportAll
  trait BlessedScreenMock {
    
    def focused: BlessedElement
  }

  @JSExportAll
  trait BlessedElementMock {
    
    def focus(): Unit
  }
  
  @JSExportAll
  trait FormElementMock {
    
    def screen: BlessedScreen

    def _selected_=(el: BlessedElement): Unit
    def focusFirst(): Unit
    def focusNext(): Unit
    def focusPrevious(): Unit
    
    def on(event: String, listener: js.Function): Unit
    def off(event: String, listener: js.Function): Unit
  }
}
