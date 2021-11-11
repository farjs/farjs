package farjs.filelist.stack

import java.util.concurrent.atomic.AtomicReference

import farjs.filelist.stack.PanelStack._
import farjs.filelist.stack.PanelStackSpec.TestParams
import scommons.react._
import scommons.react.hooks._
import scommons.react.test._

import scala.scalajs.js

class PanelStackSpec extends TestSpec with TestRendererUtils {

  PanelStack.withSizeComp = () => "WithSize".asInstanceOf[ReactClass]

  private val (width, height) = (25, 15)

  it should "push new component when push" in {
    //given
    val updater = mockFunction[js.Function1[List[StackItem], List[StackItem]], Unit]
    val stack = new PanelStack(None, updater)
    val comp = "new comp".asInstanceOf[ReactClass]
    val params = TestParams("test name")
    val data = List[StackItem](("existing comp".asInstanceOf[ReactClass], null))
    
    var result: List[StackItem] = null
    updater.expects(*).onCall { updateFn: js.Function1[List[StackItem], List[StackItem]] =>
      //when
      result = updateFn(data)
    }
    
    //when
    stack.push(comp, params)
    
    //then
    result shouldBe (comp, params) :: data
  }
  
  it should "do nothing if empty stack data when update" in {
    //given
    val updater = mockFunction[js.Function1[List[StackItem], List[StackItem]], Unit]
    val stack = new PanelStack(None, updater)
    val params = TestParams("test name")
    val data = List.empty[StackItem]
    
    var result: List[StackItem] = null
    updater.expects(*).onCall { updateFn: js.Function1[List[StackItem], List[StackItem]] =>
      //when
      result = updateFn(data)
    }
    
    //when
    stack.update(params)
    
    //then
    result should be theSameInstanceAs data
  }
  
  it should "update top component params when update" in {
    //given
    val updater = mockFunction[js.Function1[List[StackItem], List[StackItem]], Unit]
    val stack = new PanelStack(None, updater)
    val params = TestParams("test name")
    val top = "top comp".asInstanceOf[ReactClass]
    val other: StackItem = ("other comp".asInstanceOf[ReactClass], null)
    val data = List((top, null), other)
    
    var result: List[StackItem] = null
    updater.expects(*).onCall { updateFn: js.Function1[List[StackItem], List[StackItem]] =>
      //when
      result = updateFn(data)
    }
    
    //when
    stack.update(params)
    
    //then
    result shouldBe List((top, params), other)
  }
  
  it should "remove top component when pop" in {
    //given
    val updater = mockFunction[js.Function1[List[StackItem], List[StackItem]], Unit]
    val stack = new PanelStack(None, updater)
    val other: StackItem = ("other comp".asInstanceOf[ReactClass], null)
    val data = List(("top comp".asInstanceOf[ReactClass], null), other)
    
    var result: List[StackItem] = null
    updater.expects(*).onCall { updateFn: js.Function1[List[StackItem], List[StackItem]] =>
      //when
      result = updateFn(data)
    }
    
    //when
    stack.pop()
    
    //then
    result shouldBe List(other)
  }
  
  it should "return top when peek" in {
    //given
    val params = TestParams(name = "test")
    val top: StackItem = ("top comp".asInstanceOf[ReactClass], params.asInstanceOf[js.Any])
    val stack = new PanelStack(Some(top), null)
    
    //when
    val result = stack.peek
    
    //then
    result shouldBe Some(top)
  }
  
  it should "return params if Some(top) when params" in {
    //given
    val params = TestParams(name = "test")
    val top: StackItem = ("top comp".asInstanceOf[ReactClass], params.asInstanceOf[js.Any])
    val stack = new PanelStack(Some(top), null)
    
    //when
    val result = stack.params[TestParams]
    
    //then
    result shouldBe params
  }
  
  it should "return null if None when params" in {
    //given
    val stack = new PanelStack(None, null)
    
    //when
    val result = stack.params[TestParams]
    
    //then
    result shouldBe null
  }
  
  it should "fail if no context when usePanelStack" in {
    //given
    val wrapper = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        PanelStack.usePanelStack
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
        "Error: PanelStack.Context is not found." +
          "\nPlease, make sure you use PanelStack and not creating nested stacks."
      )
    )
  }
  
  it should "render top component if non-empty stack" in {
    //given
    val props = PanelStackProps(isRight = true, null)
    val leftStack = new PanelStack(None, null)
    val rightStack = new PanelStack(Some(("TestComp".asInstanceOf[ReactClass], null)), null)

    //when
    val result = renderWithSize(
      <(WithPanelStacks.Context.Provider)(^.contextValue := WithPanelStacksProps(leftStack, rightStack))(
        <(PanelStack())(^.wrapped := props)(
          <.>()("some other content")
        )
      )
    )

    //then
    result.`type` shouldBe "TestComp"
  }

  it should "render children if empty stack" in {
    //given
    val (stackCtx, stackComp) = getStackCtxHook
    val props = PanelStackProps(isRight = false, null)
    val leftStack = new PanelStack(None, null)
    val rightStack = new PanelStack(Some(("test comp".asInstanceOf[ReactClass], null)), null)

    //when
    val result = renderWithSize(
      <(WithPanelStacks.Context.Provider)(^.contextValue := WithPanelStacksProps(leftStack, rightStack))(
        <(PanelStack())(^.wrapped := props)(
          <(stackComp).empty,
          <.>()("some other content")
        )
      )
    )

    //then
    stackCtx.get() shouldBe props.copy(stack = leftStack, width = width, height = height)

    inside(result.children.toList) { case List(resCtxHook, otherContent) =>
      resCtxHook.`type` shouldBe stackComp
      otherContent shouldBe "some other content"
    }
  }

  private def renderWithSize(element: ReactElement): TestInstance = {
    val withSizeProps = findComponentProps(testRender(element), withSizeComp)
    createTestRenderer(withSizeProps.render(width, height)).root
  }
  
  private def getStackCtxHook: (AtomicReference[PanelStackProps], ReactClass) = {
    val ref = new AtomicReference[PanelStackProps](null)
    (ref, new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        val ctx = useContext(PanelStack.Context)
        ref.set(ctx)
        <.>()()
      }
    }.apply())
  }
}

object PanelStackSpec {

  private case class TestParams(name: String)
}
