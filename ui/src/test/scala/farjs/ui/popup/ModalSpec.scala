package farjs.ui.popup

import farjs.ui.popup.Modal._
import farjs.ui.theme.DefaultTheme
import org.scalatest.Assertion
import scommons.react._
import scommons.react.test._

import scala.scalajs.js

class ModalSpec extends TestSpec with TestRendererUtils {

  Modal.popupComp = "Popup".asInstanceOf[ReactClass]
  Modal.modalContentComp = mockUiComponent("ModalContent")

  it should "render component" in {
    //given
    val props = ModalProps("test title", (10, 20), DefaultTheme.popup.regular, () => ())
    val children = <.button()("some child")

    //when
    val result = testRender(<(Modal())(^.wrapped := props)(
      children
    ))

    //then
    assertModal(result, props, children)
  }

  private def assertModal(result: TestInstance, props: ModalProps, children: ReactElement): Assertion = {
    assertNativeComponent(result, <(popupComp)(^.assertPlain[PopupProps](inside(_) {
      case PopupProps(onClose, focusable, _, _) =>
        onClose.isDefined shouldBe true
        focusable shouldBe js.undefined
    }))(), inside(_) { case List(content) =>
      assertTestComponent(content, modalContentComp, plain = true)({
        case ModalContentProps(title, width, height, style, padding, left, footer) =>
          title shouldBe props.title
          (width -> height) shouldBe props.size
          style shouldBe props.style
          padding shouldBe js.undefined
          left shouldBe js.undefined
          footer shouldBe js.undefined
      }, inside(_) { case List(child) =>
        assertNativeComponent(child, children)
      })
    })
  }
}
