package farjs.ui

import scommons.react.blessed._
import scommons.react.test.TestSpec
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
    assertNativeComponent(comp.children(0), <.box()(<.text()("width: 1, height: 2")))
    boxMock.width = 2
    boxMock.height = 3

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
    val comp = <(WithSize())(^.plain := props)()
    val boxMock = literal("width" -> 1, "height" -> 2)

    //when
    val result = testRender(comp, { el =>
      if (el.`type` == "box".asInstanceOf[js.Any]) boxMock
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
