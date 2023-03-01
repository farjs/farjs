package farjs.viewer

import farjs.ui.popup.ListPopupProps
import farjs.viewer.EncodingsPopup._
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.test._

class EncodingsPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  EncodingsPopup.listPopup = mockUiComponent("ListPopup")

  it should "call onApply when onSelect" in {
    //given
    val onApply = mockFunction[String, Unit]
    val props = getEncodingsPopupProps(onApply = onApply)

    val result = createTestRenderer(<(EncodingsPopup())(^.wrapped := props)()).root

    eventually {
      findComponentProps(result, listPopup)
    }.map { popup =>
      //then
      onApply.expects("latin1")

      //when
      popup.onSelect.get(1)

      Succeeded
    }
  }

  it should "call onApply with original encoding when onClose" in {
    //given
    val onApply = mockFunction[String, Unit]
    val onClose = mockFunction[Unit]
    val props = getEncodingsPopupProps(onApply = onApply, onClose = onClose)
    val origEncoding = props.encoding

    val renderer = createTestRenderer(<(EncodingsPopup())(^.wrapped := props)())

    eventually {
      findComponentProps(renderer.root, listPopup)
    }.flatMap { popup =>
      TestRenderer.act { () =>
        renderer.update(<(EncodingsPopup())(^.wrapped := props.copy(encoding = "latin1"))())
      }
      eventually {
        val popup = findComponentProps(renderer.root, listPopup)
        popup.selected shouldBe 1
        popup
      }
    }.map { popup =>
      //then
      onApply.expects(origEncoding)
      onClose.expects()

      //when
      popup.onClose()

      Succeeded
    }
  }

  it should "not call onApply if same encoding when onClose" in {
    //given
    val onApply = mockFunction[String, Unit]
    val onClose = mockFunction[Unit]
    val props = getEncodingsPopupProps(onApply = onApply, onClose = onClose)

    val result = createTestRenderer(<(EncodingsPopup())(^.wrapped := props)()).root

    eventually {
      findComponentProps(result, listPopup)
    }.map { popup =>
      //then
      onApply.expects(*).never()
      onClose.expects()

      //when
      popup.onClose()

      Succeeded
    }
  }

  it should "call onClose when onAction" in {
    //given
    val onClose = mockFunction[Unit]
    val props = getEncodingsPopupProps(onClose = onClose)

    val result = createTestRenderer(<(EncodingsPopup())(^.wrapped := props)()).root

    eventually {
      findComponentProps(result, listPopup)
    }.map { popup =>
      //then
      onClose.expects()

      //when
      popup.onAction(1)

      Succeeded
    }
  }

  it should "render popup" in {
    //given
    val props = getEncodingsPopupProps(encoding = "latin1")
    
    //when
    val result = createTestRenderer(<(EncodingsPopup())(^.wrapped := props)()).root

    //then
    eventually {
      result.children.toList should not be empty
    }.map { _ =>
      assertEncodingsPopup(result, props, selected = 1)
    }
  }
  
  private def getEncodingsPopupProps(encoding: String = "utf-8",
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
        case ListPopupProps(title, resItems, _, _, resSelected, _, _, footer, textPaddingLeft, textPaddingRight) =>
          title shouldBe "Encodings"
          resItems shouldBe List("utf-8", "latin1")
          resSelected shouldBe selected
          footer shouldBe None
          textPaddingLeft shouldBe 2
          textPaddingRight shouldBe 1
      }))()
    ))
  }
}
