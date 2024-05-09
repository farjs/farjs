package farjs.filelist.stack

import scommons.react._
import scommons.react.blessed.BlessedElement
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
    val props = WithPanelStacksProps(
      new PanelStack(isActive = true, js.Array(), updater),
      null,
      new PanelStack(isActive = false, js.Array(), updater),
      null
    )

    //when
    val result = createTestRenderer(<(WithPanelStacks())(^.wrapped := props)(
      <(stacksComp).empty,
      <.>()("some other content")
    )).root

    //then
    stacksCtx.get() shouldBe props
    
    inside(result.children.toList) { case List(resCtxHook, otherContent) =>
      resCtxHook.`type` shouldBe stacksComp
      otherContent shouldBe "some other content"
    }
  }

  private def getStacksCtxHook: (AtomicReference[WithPanelStacksProps], ReactClass) = {
    val ref = new AtomicReference[WithPanelStacksProps](null)
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
                  leftStack: PanelStack,
                  rightStack: PanelStack,
                  leftInput: BlessedElement = null,
                  rightInput: BlessedElement = null): ReactElement = {

    <(WithPanelStacks.Context.Provider)(^.contextValue := WithPanelStacksProps(
      leftStack = leftStack,
      leftInput = leftInput,
      rightStack = rightStack,
      rightInput = rightInput
    ))(
      element
    )
  }
}
