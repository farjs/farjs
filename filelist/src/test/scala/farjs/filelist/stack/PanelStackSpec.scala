package farjs.filelist.stack

import java.util.concurrent.atomic.AtomicReference
import farjs.filelist.stack.PanelStack._
import farjs.filelist.stack.PanelStackSpec.TestParams
import scommons.react._
import scommons.react.blessed.BlessedElement
import scommons.react.hooks._
import scommons.react.test._

import scala.scalajs.js

class PanelStackSpec extends TestSpec with TestRendererUtils {

  PanelStack.withSizeComp = mockUiComponent("WithSize")

  private val (width, height) = (25, 15)

  it should "push new component when push" in {
    //given
    val updater = mockFunction[js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]], Unit]
    val stack = new PanelStack(isActive = false, Nil, updater)
    val data = List(
      PanelStackItem[TestParams]("existing comp".asInstanceOf[ReactClass], None, None, None)
    )
    val newItem = PanelStackItem("new comp".asInstanceOf[ReactClass], None, None, Some(TestParams("test name")))
    
    var result: List[PanelStackItem[_]] = null
    updater.expects(*).onCall { updateFn: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] =>
      //when
      result = updateFn(data)
    }
    
    //when
    stack.push(newItem)
    
    //then
    result shouldBe newItem :: data
  }
  
  it should "do nothing if empty stack data when update" in {
    //given
    val updater = mockFunction[js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]], Unit]
    val stack = new PanelStack(isActive = false, Nil, updater)
    val params = TestParams("test name")
    val data = List.empty[PanelStackItem[TestParams]]
    
    var result: List[PanelStackItem[_]] = null
    updater.expects(*).onCall { updateFn: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] =>
      //when
      result = updateFn(data)
    }
    
    //when
    stack.update[TestParams](_.withState(params))
    
    //then
    result should be theSameInstanceAs data
  }
  
  it should "update top item when update" in {
    //given
    val updater = mockFunction[js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]], Unit]
    val stack = new PanelStack(isActive = false, Nil, updater)
    val params = TestParams("test name")
    val top = PanelStackItem[TestParams]("top comp".asInstanceOf[ReactClass], None, None, None)
    val other = PanelStackItem("other comp".asInstanceOf[ReactClass], None, None, None)
    val data = List(top, other)
    
    var result: List[PanelStackItem[_]] = null
    updater.expects(*).onCall { updateFn: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] =>
      //when
      result = updateFn(data)
    }
    
    //when
    stack.update[TestParams](_.withState(params))
    
    //then
    result shouldBe List(top.copy(state = Some(params)), other)
  }
  
  it should "update item when updateFor" in {
    //given
    val updater = mockFunction[js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]], Unit]
    val stack = new PanelStack(isActive = false, Nil, updater)
    val params = TestParams("test name")
    val top = PanelStackItem[TestParams]("top comp".asInstanceOf[ReactClass], None, None, None)
    val component = "other comp".asInstanceOf[ReactClass]
    val other = PanelStackItem[TestParams](component, None, None, None)
    val data = List(top, other)
    
    var result: List[PanelStackItem[_]] = null
    updater.expects(*).onCall { updateFn: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] =>
      //when
      result = updateFn(data)
    }
    
    //when
    stack.updateFor[TestParams](component)(_.withState(params))
    
    //then
    result shouldBe List(top, other.copy(state = Some(params)))
  }
  
  it should "remove top component when pop" in {
    //given
    val updater = mockFunction[js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]], Unit]
    val stack = new PanelStack(isActive = false, Nil, updater)
    val other = PanelStackItem[Unit]("other comp".asInstanceOf[ReactClass], None, None, None)
    val data = List(
      PanelStackItem[TestParams]("top comp".asInstanceOf[ReactClass], None, None, None),
      other
    )
    
    var result: List[PanelStackItem[_]] = null
    updater.expects(*).onCall { updateFn: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] =>
      //when
      result = updateFn(data)
    }
    
    //when
    stack.pop()
    
    //then
    result shouldBe List(other)
  }
  
  it should "not remove last item when pop" in {
    //given
    val updater = mockFunction[js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]], Unit]
    val stack = new PanelStack(isActive = false, Nil, updater)
    val data = List(
      PanelStackItem[TestParams]("top comp".asInstanceOf[ReactClass], None, None, None)
    )
    
    var result: List[PanelStackItem[_]] = null
    updater.expects(*).onCall { updateFn: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] =>
      //when
      result = updateFn(data)
    }
    
    //when
    stack.pop()
    
    //then
    result shouldBe data
  }
  
  it should "return top item when peek" in {
    //given
    val params = TestParams(name = "test")
    val top = PanelStackItem("top comp".asInstanceOf[ReactClass], None, None, Some(params))
    val other = PanelStackItem("other comp".asInstanceOf[ReactClass], None, None, None)
    val stack = new PanelStack(isActive = false, List(top, other), null)
    
    //when
    val result = stack.peek
    
    //then
    result shouldBe top
  }
  
  it should "return last item when peekLast" in {
    //given
    val params = TestParams(name = "test")
    val top = PanelStackItem("top comp".asInstanceOf[ReactClass], None, None, Some(params))
    val other = PanelStackItem("other comp".asInstanceOf[ReactClass], None, None, None)
    val stack = new PanelStack(isActive = false, List(top, other), null)
    
    //when
    val result = stack.peekLast
    
    //then
    result shouldBe other
  }
  
  it should "return params of top item when params" in {
    //given
    val params = TestParams(name = "test")
    val top = PanelStackItem("top comp".asInstanceOf[ReactClass], None, None, Some(params))
    val other = PanelStackItem("other comp".asInstanceOf[ReactClass], None, None, None)
    val stack = new PanelStack(isActive = false, List(top, other), null)
    
    //when
    val result = stack.params[TestParams]
    
    //then
    result shouldBe params
  }
  
  it should "return null if None when params" in {
    //given
    val top = PanelStackItem[TestParams]("top comp".asInstanceOf[ReactClass], None, None, None)
    val stack = new PanelStack(isActive = false, List(top), null)
    
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
  
  it should "render top component and children" in {
    //given
    val (stackCtx, stackComp) = getStackCtxHook
    val top = PanelStackItem[TestParams]("TopComp".asInstanceOf[ReactClass], None, None, None)
    val other = PanelStackItem[TestParams]("OtherComp".asInstanceOf[ReactClass], None, None, None)
    val stack = new PanelStack(isActive = false, List(top, other), null)
    val props = PanelStackProps(isRight = true, panelInput = null, stack)

    //when
    val result = renderWithSize(
      <(PanelStack())(^.wrapped := props)(
        <(stackComp).empty
      )
    )

    //then
    stackCtx.get() shouldBe props.copy(width = width, height = height)
    
    inside(result.children.toList) { case List(resTopComp, resCtxHook) =>
      resTopComp.`type` shouldBe "TopComp"
      resCtxHook.`type` shouldBe stackComp
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

  def withContext(element: ReactElement,
                  isRight: Boolean = false,
                  panelInput: BlessedElement = null,
                  stack: PanelStack = null,
                  width: Int = 0,
                  height: Int = 0
                 ): ReactElement = {

    <(PanelStack.Context.Provider)(^.contextValue := PanelStackProps(
      isRight = isRight,
      panelInput = panelInput,
      stack = stack,
      width = width,
      height = height
    ))(
      element
    )
  }
}
