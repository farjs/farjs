package scommons.farc.ui.popup

import scommons.farc.ui.popup.PopupOverlaySpec._
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
    
    //then
    (formMock.focusFirst _).expects().never()
    onOpen.expects()

    //when
    createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else fail(s"Need mock for: ${el.`type`}")
    })
  }

  it should "focus focused element and remove listener when unmount" in {
    //given
    val props = PopupProps(onClose = () => (), focusable = false)
    val screenMock = mock[BlessedScreenMock]
    val formMock = mock[FormElementMock]
    val focused = mock[BlessedElementMock]
    var capturedListener: js.Function3[BlessedElement, js.Object, KeyboardKey, Unit] = null

    (formMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.focused _).expects().returning(focused.asInstanceOf[BlessedElement])
    (formMock.on _).expects("element keypress", *).onCall { (_, listener) =>
      capturedListener = listener
    }
    
    val renderer = createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else fail(s"Need mock for: ${el.`type`}")
    })

    //then
    (focused.focus _).expects()
    (formMock.off _).expects(where { (event, listener) =>
      event == "element keypress" && listener == capturedListener
    })

    //when
    renderer.unmount()
  }

  it should "only remove listener if no focused element when unmount" in {
    //given
    val props = PopupProps(onClose = () => (), focusable = false)
    val screenMock = mock[BlessedScreenMock]
    val formMock = mock[FormElementMock]

    (formMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.focused _).expects().returning(null)
    (formMock.on _).expects("element keypress", *)
    
    val renderer = createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else fail(s"Need mock for: ${el.`type`}")
    })

    //then
    (formMock.off _).expects("element keypress", *)

    //when
    renderer.unmount()
  }

  it should "re-subscribe element listener when update" in {
    //given
    val props = PopupProps(onClose = () => (), focusable = false)
    val screenMock = mock[BlessedScreenMock]
    val formMock = mock[FormElementMock]

    (formMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.focused _).expects().returning(null)
    (formMock.on _).expects("element keypress", *)
    
    val renderer = createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else fail(s"Need mock for: ${el.`type`}")
    })

    //then
    (formMock.on _).expects("element keypress", *)
    (formMock.off _).expects("element keypress", *)

    //when
    renderer.update(<(PopupOverlay())(^.wrapped := props.copy(onClose = () => ()))())
  }

  it should "call onClose if closable when escape" in {
    //given
    val onClose = mockFunction[Unit]
    val props = PopupProps(onClose = onClose, focusable = false)
    val screenMock = mock[BlessedScreenMock]
    val formMock = mock[FormElementMock]

    (formMock.screen _).expects().returning(screenMock.asInstanceOf[BlessedScreen])
    (screenMock.focused _).expects().returning(null)
    
    //then
    onClose.expects()

    (formMock.on _).expects("element keypress", *).onCall { (_, listener) =>
      //when
      listener(null, null, js.Dynamic.literal("full" -> "escape").asInstanceOf[KeyboardKey])
    }

    //when
    createTestRenderer(<(PopupOverlay())(^.wrapped := props)(), { el =>
      if (el.`type` == "form".asInstanceOf[js.Any]) formMock.asInstanceOf[js.Any]
      else fail(s"Need mock for: ${el.`type`}")
    })
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
      listener(null, null, js.Dynamic.literal("full" -> "escape").asInstanceOf[KeyboardKey])
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

    def focusFirst(): Unit
    def on(event: String, listener: js.Function3[BlessedElement, js.Object, KeyboardKey, Unit]): Unit
    def off(event: String, listener: js.Function3[BlessedElement, js.Object, KeyboardKey, Unit]): Unit
  }
}
