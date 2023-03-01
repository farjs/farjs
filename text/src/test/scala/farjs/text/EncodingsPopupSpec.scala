package farjs.text

import farjs.text.EncodingsPopup._
import farjs.ui.popup.ListPopupProps
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

class EncodingsPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  EncodingsPopup.listPopup = mockUiComponent("ListPopup")

  it should "call onApply with new encoding when onAction" in {
    //given
    val onApply = mockFunction[String, Unit]
    val onClose = mockFunction[Unit]
    val props = getEncodingsPopupProps(onApply = onApply, onClose = onClose)

    val renderer = createTestRenderer(<(EncodingsPopup())(^.wrapped := props)())

    eventually {
      findComponentProps(renderer.root, listPopup)
    }.map { popup =>
      //then
      onApply.expects("big5")
      onClose.expects()

      //when
      popup.onAction(1)

      Succeeded
    }
  }

  it should "not call onApply if same encoding when onAction" in {
    //given
    val onApply = mockFunction[String, Unit]
    val onClose = mockFunction[Unit]
    val props = getEncodingsPopupProps(encoding = "big5", onApply = onApply, onClose = onClose)

    val result = createTestRenderer(<(EncodingsPopup())(^.wrapped := props)()).root

    eventually {
      findComponentProps(result, listPopup)
    }.map { popup =>
      //then
      onApply.expects(*).never()
      onClose.expects()

      //when
      popup.onAction(1)

      Succeeded
    }
  }

  it should "render popup" in {
    //given
    val props = getEncodingsPopupProps(encoding = "big5")
    
    //when
    val result = createTestRenderer(<(EncodingsPopup())(^.wrapped := props)()).root

    //then
    eventually {
      result.children.toList should not be empty
    }.map { _ =>
      assertEncodingsPopup(result, props, selected = 1)
    }
  }
  
  private def getEncodingsPopupProps(encoding: String = "utf8",
                                     onApply: String => Unit = _ => (),
                                     onClose: () => Unit = () => ()): EncodingsPopupProps = {
    EncodingsPopupProps(
      encoding = encoding,
      onApply = onApply,
      onClose = onClose
    )
  }

  private def assertEncodingsPopup(result: TestInstance,
                                   props: EncodingsPopupProps,
                                   selected: Int): Assertion = {

    assertComponents(result.children, List(
      <(listPopup())(^.assertWrapped(inside(_) {
        case ListPopupProps(title, resItems, _, onClose, resSelected, _, _, footer, textPaddingLeft, textPaddingRight) =>
          title shouldBe "Encodings"
          resItems shouldBe Encoding.encodings
          onClose should be theSameInstanceAs props.onClose
          resSelected shouldBe selected
          footer shouldBe None
          textPaddingLeft shouldBe 2
          textPaddingRight shouldBe 1
      }))()
    ))
  }
}
