package farjs.ui

import farjs.ui.popup.PopupOverlay
import scommons.react.blessed._
import scommons.react.test.{TestInstance, TestSpec}
import scommons.react.test.raw.TestRenderer
import scommons.react.test.util.TestRendererUtils

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal

class WithSizeSpec extends TestSpec with TestRendererUtils {

  it should "re-render with new size when onResize" in {
    //given
    val props = WithSizeProps({ (width, height) =>
      <.text()(s"width: $width, height: $height")
    })
    val boxMock = literal("width" -> 1, "height" -> 2)
    val comp = createTestRenderer(<(WithSize())(^.plain := props)(), { el =>
      if (el.`type` == "box".asInstanceOf[js.Any]) boxMock
      else null
    }).root
    assertWithSize(comp.children(0), width = 1, height = 2)
    boxMock.width = 2
    boxMock.height = 3

    //when
    TestRenderer.act { () =>
      comp.children(0).props.onResize()
    }

    //then
    assertWithSize(comp.children(0), width = 2, height = 3)
  }
  
  it should "render component with size" in {
    //given
    val props = WithSizeProps({ (width, height) =>
      <.text()(s"width: $width, height: $height")
    })
    val comp = <(WithSize())(^.plain := props)()
    val boxMock = literal("width" -> 1, "height" -> 2)

    //when
    val result = testRender(comp, { el =>
      if (el.`type` == "box".asInstanceOf[js.Any]) boxMock
      else null
    })

    //then
    assertWithSize(result, width = 1, height = 2)
  }
  
  private def assertWithSize(result: TestInstance, width: Int, height: Int): Unit = {
    assertNativeComponent(result,
      <.box(
        ^.rbStyle := PopupOverlay.style
      )(
        <.text()(s"width: $width, height: $height")
      )
    )
  }
}
