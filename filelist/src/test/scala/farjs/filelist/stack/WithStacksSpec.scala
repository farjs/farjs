package farjs.filelist.stack

import farjs.filelist.stack.WithStacksSpec.assertStacks
import org.scalatest.Assertion
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import scommons.react._
import scommons.react.hooks._
import scommons.react.test._

import java.util.concurrent.atomic.AtomicReference
import scala.scalajs.js

class WithStacksSpec extends TestSpec with TestRendererUtils {

  it should "fail if no context when useStacks" in {
    //given
    val wrapper = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        WithStacks.useStacks()
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
        "Error: WithStacks.Context is not found." +
          "\nPlease, make sure you use WithStacks.Context.Provider in parent component."
      )
    )
  }
  
  it should "render component with context provider" in {
    //given
    val (stacksCtx, stacksComp) = getStacksCtxHook
    val updater: js.Function1[js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]], Unit] = { _ =>
    }
    val props = WithStacksProps(
      WithStacksData(new PanelStack(isActive = true, js.Array(), updater), null),
      WithStacksData(new PanelStack(isActive = false, js.Array(), updater), null)
    )

    //when
    val result = createTestRenderer(<(WithStacks())(^.plain := props)(
      <(stacksComp).empty,
      <.>()("some other content")
    )).root

    //then
    assertStacks(stacksCtx.get(), props)
    
    inside(result.children.toList) { case List(resCtxHook, otherContent) =>
      resCtxHook.`type` shouldBe stacksComp
      otherContent shouldBe "some other content"
    }
  }

  private def getStacksCtxHook: (AtomicReference[WithStacksProps], ReactClass) = {
    val ref = new AtomicReference[WithStacksProps](null)
    (ref, new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        val ctx = useContext(WithStacks.Context)
        ref.set(ctx)
        <.>()()
      }
    }.apply())
  }
}

object WithStacksSpec {

  def withContext(element: ReactElement,
                  left: WithStacksData,
                  right: WithStacksData): ReactElement = {

    <(WithStacks.Context.Provider)(^.contextValue := WithStacksProps(
      left = left,
      right = right
    ))(
      element
    )
  }
  
  def assertStacks(result: WithStacksProps, expected: WithStacksProps): Assertion = {
    inside(result) {
      case WithStacksProps(left, right) =>
        left shouldBe expected.left
        right shouldBe expected.right
    }
  }
}
