package farjs.ui.popup

import farjs.ui.popup.Popup._
import scommons.react._
import scommons.react.blessed._
import scommons.react.test._

class PopupSpec extends TestSpec with TestRendererUtils {

  Popup.portalComp = () => "Portal".asInstanceOf[ReactClass]
  Popup.popupOverlayComp = () => "PopupOverlay".asInstanceOf[ReactClass]

  it should "render component" in {
    //given
    val children: ReactElement = <.box()("test popup child")
    val props = PopupProps(onClose = () => ())

    //when
    val result = testRender(<(Popup())(^.wrapped := props)(children))

    //then
    assertPopup(result, props, children)
  }

  private def assertPopup(result: TestInstance,
                          props: PopupProps,
                          children: ReactElement): Unit = {
    
    assertNativeComponent(result, <(portalComp()).empty, { case List(content) =>
      assertTestComponent(content, popupOverlayComp)({ resProps =>
        resProps should be theSameInstanceAs props
      }, {
        case List(child) => assertNativeComponent(child, children)
      })
    })
  }
}
