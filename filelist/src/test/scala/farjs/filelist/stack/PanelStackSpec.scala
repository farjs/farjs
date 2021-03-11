package farjs.filelist.stack

import java.util.concurrent.atomic.AtomicReference

import farjs.filelist.stack.PanelStack.StackItem
import farjs.filelist.stack.PanelStackSpec.TestParams
import farjs.filelist.stack.WithPanelStacksSpec.TestErrorBoundary
import scommons.react._
import scommons.react.hooks._
import scommons.react.test._

import scala.scalajs.js

class PanelStackSpec extends TestSpec with TestRendererUtils {

  it should "push new component when push" in {
    //given
    val updater = mockFunction[js.Function1[List[StackItem], List[StackItem]], Unit]
    val stack = new PanelStack(None, updater)
    val comp = "new comp".asInstanceOf[ReactClass]
    val params = js.Dynamic.literal()
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
    val params = js.Dynamic.literal()
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
  
  it should "update top component when update" in {
    //given
    val updater = mockFunction[js.Function1[List[StackItem], List[StackItem]], Unit]
    val stack = new PanelStack(None, updater)
    val params = js.Dynamic.literal()
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
    
    //when
    val result = testRender(<(TestErrorBoundary())()(
      <(wrapper()).empty
    ))
    
    //then
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
    val result = createTestRenderer(
      <(WithPanelStacks.Context.Provider)(^.contextValue := WithPanelStacksProps(leftStack, rightStack))(
        <(PanelStack())(^.wrapped := props)(
          <.>()("some other content")
        )
      )
    ).root

    //then
    inside(result.children.toList) { case List(top) =>
      top.`type` shouldBe "TestComp"
    }
  }

  it should "render children if empty stack" in {
    //given
    val (stackCtx, stackComp) = getStackCtxHook
    val props = PanelStackProps(isRight = false, null)
    val leftStack = new PanelStack(None, null)
    val rightStack = new PanelStack(Some(("test comp".asInstanceOf[ReactClass], null)), null)

    //when
    val result = createTestRenderer(
      <(WithPanelStacks.Context.Provider)(^.contextValue := WithPanelStacksProps(leftStack, rightStack))(
        <(PanelStack())(^.wrapped := props)(
          <(stackComp).empty,
          <.>()("some other content")
        )
      )
    ).root

    //then
    stackCtx.get() shouldBe props

    inside(result.children.toList) { case List(resCtxHook, otherContent) =>
      resCtxHook.`type` shouldBe stackComp
      otherContent shouldBe "some other content"
    }
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
