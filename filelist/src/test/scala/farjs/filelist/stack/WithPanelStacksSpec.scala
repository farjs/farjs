package farjs.filelist.stack

import farjs.filelist.stack.WithPanelStacksSpec.assertPanelStacks
import org.scalatest.Assertion
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import scommons.react._
import scommons.react.hooks._
import scommons.react.test._

import java.util.concurrent.atomic.AtomicReference
import scala.scalajs.js

class WithPanelStacksSpec extends TestSpec with TestRendererUtils {

  it should "fail if no context when usePanelStacks" in {
    //given
    val wrapper = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        WithPanelStacks.usePanelStacks
        <.>()()
      }
    }
    
    // suppress intended error
    // see: https://github.com/facebook/react/issues/11098#issuecomment-412682721
    val savedConsoleError = js.Dynamic.global.console.error
    js.Dynamic.global.console.error = { _: js.Any =>
    }

    //when
    val result = testRender(<(TestErrorBoundary())()(
      <(wrapper()).empty
    ))
    
    //then
    js.Dynamic.global.console.error = savedConsoleError
    
    assertNativeComponent(result,
      <.div()(
        "Error: WithPanelStacks.Context is not found." +
          "\nPlease, make sure you use WithPanelStacks.Context.Provider in parent component."
      )
    )
  }
  
  it should "render component with context provider" in {
    //given
    val (stacksCtx, stacksComp) = getStacksCtxHook
    val updater: js.Function1[js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]], Unit] = { _ =>
    }
    val props = PanelStacks(
      PanelStackData(new PanelStack(isActive = true, js.Array(), updater), null),
      PanelStackData(new PanelStack(isActive = false, js.Array(), updater), null)
    )

    //when
    val result = createTestRenderer(<(WithPanelStacks())(^.plain := props)(
      <(stacksComp).empty,
      <.>()("some other content")
    )).root

    //then
    assertPanelStacks(stacksCtx.get(), props)
    
    inside(result.children.toList) { case List(resCtxHook, otherContent) =>
      resCtxHook.`type` shouldBe stacksComp
      otherContent shouldBe "some other content"
    }
  }

  private def getStacksCtxHook: (AtomicReference[PanelStacks], ReactClass) = {
    val ref = new AtomicReference[PanelStacks](null)
    (ref, new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        val ctx = useContext(WithPanelStacks.Context)
        ref.set(ctx)
        <.>()()
      }
    }.apply())
  }
}

object WithPanelStacksSpec {

  def withContext(element: ReactElement,
                  left: PanelStackData,
                  right: PanelStackData): ReactElement = {

    <(WithPanelStacks.Context.Provider)(^.contextValue := PanelStacks(
      left = left,
      right = right
    ))(
      element
    )
  }
  
  def assertPanelStacks(result: PanelStacks, expected: PanelStacks): Assertion = {
    inside(result) {
      case PanelStacks(left, right) =>
        left shouldBe expected.left
        right shouldBe expected.right
    }
  }
}
