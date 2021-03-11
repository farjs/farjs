package farjs.filelist.stack

import java.util.concurrent.atomic.AtomicReference

import farjs.filelist.stack.PanelStack.StackItem
import farjs.filelist.stack.WithPanelStacksSpec.TestErrorBoundary
import scommons.react._
import scommons.react.hooks._
import scommons.react.test._

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
    
    //when
    val result = testRender(<(TestErrorBoundary())()(
      <(wrapper()).empty
    ))
    
    //then
    assertNativeComponent(result,
      <.div()(
        "Error: WithPanelStacks.Context is not found." +
          "\nPlease, make sure you use WithPanelStacks and not creating nested stacks."
      )
    )
  }
  
  it should "render component with context provider" in {
    //given
    val (stacksCtx, stacksComp) = getStacksCtxHook
    val updater: js.Function1[js.Function1[List[StackItem], List[StackItem]], Unit] = { _ =>
    }
    val props = WithPanelStacksProps(new PanelStack(None, updater), new PanelStack(None, updater))

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

  object TestErrorBoundary extends ClassComponent[Unit] {
    
    private case class TestErrorBoundaryState(error: Option[js.Object] = None)

    protected def create(): ReactClass = createClass[TestErrorBoundaryState](
      getInitialState = { _ =>
        TestErrorBoundaryState()
      },
      componentDidCatch = { (self, error, _) =>
        self.setState(TestErrorBoundaryState(Option(error)))
      },
      render = { self =>
        self.state.error match {
          case None => self.props.children
          case Some(error) =>
            <.div()(
              s"$error"
            )
        }
      }
    )
  }
}
