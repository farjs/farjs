package farclone.ui.menu

import farclone.ui._
import scommons.react._
import scommons.react.test.TestSpec
import scommons.react.test.raw.ShallowInstance
import scommons.react.test.util.ShallowRendererUtils

class BottomMenuSpec extends TestSpec with ShallowRendererUtils {

  it should "render component" in {
    //when
    val result = shallowRender(<(BottomMenu())()())

    //then
    assertBottomMenu(result)
  }
  
  private def assertBottomMenu(result: ShallowInstance): Unit = {
    val (width, height) = (80, 25)

    def renderContent(content: ReactElement): ShallowInstance = {
      val wrapper = new ClassComponent[Unit] {
        protected def create(): ReactClass = createClass[Unit](_ => content)
      }

      shallowRender(<(wrapper()).empty)
    }

    assertComponent(result, WithSize) { case WithSizeProps(render) =>
      val result = renderContent(render(width, height))

      assertComponent(result, BottomMenuView) { case BottomMenuViewProps(resWidth, resItems) =>
        resWidth shouldBe width
        resItems shouldBe BottomMenu.items
      }
    }
  }
}
