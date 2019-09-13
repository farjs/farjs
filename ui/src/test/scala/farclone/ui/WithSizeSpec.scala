package farclone.ui

import farclone.ui.WithSizeSpec._
import scommons.react.blessed._
import scommons.react.test.TestSpec
import scommons.react.test.raw.TestRenderer
import scommons.react.test.util.TestRendererUtils

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll

class WithSizeSpec extends TestSpec with TestRendererUtils {

  it should "re-render with new size when onResize" in {
    //given
    val props = WithSizeProps({ (width, height) =>
      <.text()(s"width: $width, height: $height")
    })
    val boxMock = mock[BlessedElementMock]
    inSequence {
      inAnyOrder {
        (boxMock.width _).expects().returning(1).twice()
        (boxMock.height _).expects().returning(2).twice()
      }
      inAnyOrder {
        (boxMock.width _).expects().returning(2).twice()
        (boxMock.height _).expects().returning(3).twice()
      }
    }
    val comp = createTestRenderer(<(WithSize())(^.wrapped := props)(), { el =>
      if (el.`type` == "box".asInstanceOf[js.Any]) {
        boxMock.asInstanceOf[BlessedElement]
      }
      else null
    }).root
    assertNativeComponent(comp.children(0), <.box()(<.text()("width: 1, height: 2")))

    //when
    TestRenderer.act { () =>
      comp.children(0).props.onResize()
    }

    //then
    assertNativeComponent(comp.children(0), <.box()(<.text()("width: 2, height: 3")))
  }
  
  it should "render component with size" in {
    //given
    val props = WithSizeProps({ (width, height) =>
      <.text()(s"width: $width, height: $height")
    })
    val comp = <(WithSize())(^.wrapped := props)()
    val boxMock = mock[BlessedElementMock]
    (boxMock.width _).expects().returning(1).twice()
    (boxMock.height _).expects().returning(2).twice()

    //when
    val result = testRender(comp, { el =>
      if (el.`type` == "box".asInstanceOf[js.Any]) {
        boxMock.asInstanceOf[BlessedElement]
      }
      else null
    })

    //then
    assertNativeComponent(result,
      <.box()(
        <.text()("width: 1, height: 2")
      )
    )
  }
}

object WithSizeSpec {

  @JSExportAll
  trait BlessedElementMock {
    
    def width: Int
    def height: Int
  }
}
