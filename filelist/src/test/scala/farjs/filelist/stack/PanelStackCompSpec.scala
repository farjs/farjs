package farjs.filelist.stack

import farjs.filelist.stack.PanelStackComp._
import farjs.filelist.stack.PanelStackCompSpec.TestParams
import farjs.ui.WithSizeProps
import scommons.react._
import scommons.react.blessed.BlessedElement
import scommons.react.hooks._
import scommons.react.test._

import java.util.concurrent.atomic.AtomicReference
import scala.scalajs.js

class PanelStackCompSpec extends TestSpec with TestRendererUtils {

  PanelStackComp.withSizeComp = "WithSize".asInstanceOf[ReactClass]

  private val (width, height) = (25, 15)

  it should "fail if no context when usePanelStack" in {
    //given
    val wrapper = new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        PanelStackComp.usePanelStack
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
        "Error: PanelStackComp.Context is not found." +
          "\nPlease, make sure you use PanelStackComp.Context.Provider in parent component."
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
      <(PanelStackComp())(^.wrapped := props)(
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
    val withSizeProps = inside(findComponents(testRender(element), withSizeComp)) {
      case List(comp) => comp.props.asInstanceOf[WithSizeProps]
    }
    createTestRenderer(withSizeProps.render(width, height)).root
  }
  
  private def getStackCtxHook: (AtomicReference[PanelStackProps], ReactClass) = {
    val ref = new AtomicReference[PanelStackProps](null)
    (ref, new FunctionComponent[Unit] {
      protected def render(props: Props): ReactElement = {
        val ctx = useContext(PanelStackComp.Context)
        ref.set(ctx)
        <.>()()
      }
    }.apply())
  }
}

object PanelStackCompSpec {

  private case class TestParams(name: String)

  def withContext(element: ReactElement,
                  isRight: Boolean = false,
                  panelInput: BlessedElement = null,
                  stack: PanelStack = null,
                  width: Int = 0,
                  height: Int = 0
                 ): ReactElement = {

    <(PanelStackComp.Context.Provider)(^.contextValue := PanelStackProps(
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
