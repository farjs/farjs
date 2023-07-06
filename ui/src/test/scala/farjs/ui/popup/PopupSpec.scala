package farjs.ui.popup

import farjs.ui.popup.Popup._
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

import scala.scalajs.js

class PopupSpec extends TestSpec with TestRendererUtils {

  Popup.portalComp = "Portal".asInstanceOf[ReactClass]
  Popup.popupOverlayComp = mockUiComponent("PopupOverlay")

  it should "render component" in {
    //given
    val children: ReactElement = <.box()("test popup child")
    val props = PopupProps(onClose = (() => ()): js.Function0[Unit])

    //when
    val result = testRender(<(Popup())(^.plain := props)(children))

    //then
    assertPopup(result, props, children)
  }

  private def assertPopup(result: TestInstance,
                          props: PopupProps,
                          children: ReactElement): Unit = {
    
    assertNativeComponent(result, <(portalComp)()(
      <(popupOverlayComp())(^.assertPlain[PopupProps](inside(_) {
        case PopupProps(onClose, focusable, onOpen, onKeypress) =>
          onClose shouldBe props.onClose
          focusable shouldBe props.focusable
          onOpen shouldBe props.onOpen
          onKeypress shouldBe props.onKeypress
      }))(children)
    ))
  }
}
