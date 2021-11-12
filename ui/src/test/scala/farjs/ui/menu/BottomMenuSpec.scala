package farjs.ui.menu

import farjs.ui._
import farjs.ui.menu.BottomMenu._
import scommons.react.test._

class BottomMenuSpec extends TestSpec with TestRendererUtils {

  BottomMenu.withSizeComp = mockUiComponent("WithSize")
  BottomMenu.bottomMenuViewComp = mockUiComponent("BottomMenuView")

  it should "render component" in {
    //when
    val result = testRender(<(BottomMenu())()())

    //then
    assertBottomMenu(result)
  }
  
  private def assertBottomMenu(result: TestInstance): Unit = {
    val (width, height) = (80, 25)

    assertTestComponent(result, withSizeComp) { case WithSizeProps(render) =>
      val result = createTestRenderer(render(width, height)).root

      assertTestComponent(result, bottomMenuViewComp) { case BottomMenuViewProps(resWidth, resItems) =>
        resWidth shouldBe width
        resItems shouldBe BottomMenu.items
      }
    }
  }
}
