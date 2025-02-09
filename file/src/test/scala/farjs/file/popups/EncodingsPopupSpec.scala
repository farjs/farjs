package farjs.file.popups

import farjs.file.Encoding
import farjs.file.popups.EncodingsPopup._
import farjs.ui.popup.ListPopupProps
import org.scalatest.{Assertion, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass
import scommons.react.test._

import scala.scalajs.js

class EncodingsPopupSpec extends AsyncTestSpec with BaseTestSpec with TestRendererUtils {

  EncodingsPopup.listPopup = "ListPopup".asInstanceOf[ReactClass]

  it should "call onApply with new encoding when onAction" in {
    //given
    val onApply = mockFunction[String, Unit]
    val onClose = mockFunction[Unit]
    val props = getEncodingsPopupProps(onApply = onApply, onClose = onClose)

    val renderer = createTestRenderer(<(EncodingsPopup())(^.plain := props)())

    eventually {
      inside(findComponents(renderer.root, listPopup)) {
        case List(c) => c.props.asInstanceOf[ListPopupProps]
      }
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

    val result = createTestRenderer(<(EncodingsPopup())(^.plain := props)()).root

    eventually {
      inside(findComponents(result, listPopup)) {
        case List(c) => c.props.asInstanceOf[ListPopupProps]
      }
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
    val result = createTestRenderer(<(EncodingsPopup())(^.plain := props)()).root

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
      <(listPopup)(^.assertPlain[ListPopupProps](inside(_) {
        case ListPopupProps(
          title,
          resItems,
          _,
          _,
          resSelected,
          _,
          _,
          footer,
          textPaddingLeft,
          textPaddingRight,
          itemWrapPrefixLen
        ) =>
          title shouldBe "Encodings"
          resItems shouldBe Encoding.encodings
          resSelected shouldBe selected
          footer shouldBe js.undefined
          textPaddingLeft shouldBe js.undefined
          textPaddingRight shouldBe js.undefined
          itemWrapPrefixLen shouldBe js.undefined
      }))()
    ))
  }
}
