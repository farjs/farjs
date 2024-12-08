package farjs.filelist.stack

import farjs.filelist.stack.WithStack._
import farjs.filelist.stack.WithStackSpec._
import farjs.ui.WithSizeProps
import org.scalatest.Assertion
import org.scalatest.Inside.inside
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import scommons.react._
import scommons.react.blessed.BlessedElement
import scommons.react.hooks._
import scommons.react.test._

import java.util.concurrent.atomic.AtomicReference
import scala.scalajs.js

class WithStackSpec extends TestSpec with TestRendererUtils {

  WithStack.withSizeComp = "WithSize".asInstanceOf[ReactClass]

  private val (width, height) = (25, 15)

  it should "fail if no context when useStack" in {
    //given
    val wrapper = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        WithStack.useStack()
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
        "Error: WithStack.Context is not found." +
          "\nPlease, make sure you use WithStack.Context.Provider in parent component."
      )
    )
  }
  
  it should "render top component and children" in {
    //given
    val (stackCtx, stackComp) = getStackCtxHook
    val top = PanelStackItem[TestParams]("TopComp".asInstanceOf[ReactClass])
    val other = PanelStackItem[TestParams]("OtherComp".asInstanceOf[ReactClass])
    val stack = new PanelStack(isActive = false, js.Array(top, other), null)
    val props = WithStackProps(isRight = true, panelInput = null, stack)

    //when
    val result = renderWithSize(
      <(WithStack())(^.plain := props)(
        <(stackComp).empty
      )
    )

    //then
    assertStackProps(stackCtx.get(), WithStackProps.copy(props)(width = width, height = height))
    
    inside(result.children.toList) { case List(resTopComp, resCtxHook) =>
      resTopComp.`type` shouldBe "TopComp"
      resCtxHook.`type` shouldBe stackComp
    }
  }

  private def renderWithSize(element: ReactElement): TestInstance = {
    val withSizeProps = inside(findComponents(testRender(element), withSizeComp)) {
      case List(comp) => comp.props.asInstanceOf[WithSizeProps]
    }
    createTestRenderer(withSizeProps.render(width, height)).root
  }
  
  private def getStackCtxHook: (AtomicReference[WithStackProps], ReactClass) = {
    val ref = new AtomicReference[WithStackProps](null)
    (ref, new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        val ctx = useContext(WithStack.Context)
        ref.set(ctx)
        <.>()()
      }
    }.apply())
  }
}

object WithStackSpec {

  private case class TestParams(name: String)

  def withContext(element: ReactElement,
                  isRight: Boolean = false,
                  panelInput: BlessedElement = null,
                  stack: PanelStack = null,
                  width: Int = 0,
                  height: Int = 0
                 ): ReactElement = {

    <(WithStack.Context.Provider)(^.contextValue := WithStackProps(
      isRight = isRight,
      panelInput = panelInput,
      stack = stack,
      width = width,
      height = height
    ))(
      element
    )
  }

  def assertStackProps(result: WithStackProps, expected: WithStackProps): Assertion = {
    inside(result) {
      case WithStackProps(isRight, panelInput, stack, width, height) =>
        isRight shouldBe expected.isRight
        panelInput shouldBe expected.panelInput
        stack shouldBe expected.stack
        width shouldBe expected.width
        height shouldBe expected.height
    }
  }
}
