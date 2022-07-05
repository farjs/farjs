package farjs.ui.popup

import farjs.ui.popup.Modal._
import farjs.ui.theme.Theme
import org.scalatest.Assertion
import scommons.react._
import scommons.react.test._

class ModalSpec extends TestSpec with TestRendererUtils {

  Modal.popupComp = mockUiComponent("Popup")
  Modal.modalContentComp = mockUiComponent("ModalContent")

  it should "render component" in {
    //given
    val props = ModalProps("test title", (10, 20), Theme.current.popup.regular, () => ())
    val children = <.button()("some child")

    //when
    val result = testRender(<(Modal())(^.wrapped := props)(
      children
    ))

    //then
    assertModal(result, props, children)
  }

  private def assertModal(result: TestInstance, props: ModalProps, children: ReactElement): Assertion = {
    assertTestComponent(result, popupComp)({ case PopupProps(onClose, resClosable, focusable, _, _) =>
      resClosable shouldBe true
      focusable shouldBe true
      onClose should be theSameInstanceAs props.onCancel
    }, inside(_) { case List(content) =>
      assertTestComponent(content, modalContentComp)({
        case ModalContentProps(title, size, style, padding, left) =>
          title shouldBe props.title
          size shouldBe props.size
          style shouldBe props.style
          padding shouldBe ModalContent.padding
          left shouldBe "center"
      }, inside(_) { case List(child) =>
        assertNativeComponent(child, children)
      })
    })
  }
}
