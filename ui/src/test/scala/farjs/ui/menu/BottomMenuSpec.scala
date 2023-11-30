package farjs.ui.menu

import farjs.ui._
import farjs.ui.menu.BottomMenu._
import scommons.react.ReactClass
import scommons.react.test._

import scala.scalajs.js

class BottomMenuSpec extends TestSpec with TestRendererUtils {

  BottomMenu.withSizeComp = "WithSize".asInstanceOf[ReactClass]
  BottomMenu.bottomMenuViewComp = mockUiComponent("BottomMenuView")

  it should "render component" in {
    //when
    val props = BottomMenuProps(js.Array(List.fill(12)("item"): _*))
    val result = testRender(<(BottomMenu())(^.plain := props)())

    //then
    assertBottomMenu(result, props)
  }
  
  private def assertBottomMenu(result: TestInstance, props: BottomMenuProps): Unit = {
    val (width, height) = (80, 25)

    assertNativeComponent(result, <(withSizeComp)(^.assertPlain[WithSizeProps](inside(_) {
      case WithSizeProps(render) =>
        val result = createTestRenderer(render(width, height)).root

        assertTestComponent(result, bottomMenuViewComp, plain = true) { case BottomMenuViewProps(resWidth, resItems) =>
          resWidth shouldBe width
          resItems.toList shouldBe props.items.toList
        }
    }))())
  }
}
