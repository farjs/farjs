package scommons.farc.ui.popup

import scommons.react._
import scommons.react.blessed._
import scommons.react.blessed.portal.{Portal, PortalProps}
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.ShallowRendererUtils

class PopupSpec extends TestSpec with ShallowRendererUtils {

  it should "render component" in {
    //given
    val children: ReactElement = <.box()("test popup child")
    val props = PopupProps(onClose = () => ())

    //when
    val result = shallowRender(<(Popup())(^.wrapped := props)(children))

    //then
    assertPopup(result, props, children)
  }

  private def renderPortalContent(content: ReactElement): ShallowInstance = {
    val wrapper = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        content
      }
    }

    shallowRender(<(wrapper())()())
  }

  private def assertPopup(result: ShallowInstance,
                          props: PopupProps,
                          children: ReactElement): Unit = {
    
    assertComponent(result, Portal) { case PortalProps(content) =>
      assertComponent(renderPortalContent(content), PopupOverlay)({ resProps =>
        resProps shouldBe props
      }, {
        case List(child) => child shouldBe children
      })
    }
  }
}
