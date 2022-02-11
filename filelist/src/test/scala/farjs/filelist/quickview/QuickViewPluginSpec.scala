package farjs.filelist.quickview

import farjs.filelist._
import farjs.filelist.stack.{MockPanelStack, PanelStackItem}
import io.github.shogowada.scalajs.reactjs.React.Props
import scommons.react.ReactClass
import scommons.react.redux.Dispatch
import scommons.react.test.TestSpec

class QuickViewPluginSpec extends TestSpec {
  
  private def createQuickViewPlugin(actions: FileListActions): QuickViewPlugin = {
    new QuickViewPlugin(actions) {
      override def apply(): ReactClass = "QuickViewPlugin".asInstanceOf[ReactClass]
    }
  }

  //noinspection TypeAnnotation
  class Stack {
    val push = mockFunction[PanelStackItem[QuickViewParams], Unit]
    val update = mockFunction[PanelStackItem[QuickViewParams] => PanelStackItem[QuickViewParams], Unit]
    val pop = mockFunction[Unit]
    val peek = mockFunction[PanelStackItem[QuickViewParams]]
    val params = mockFunction[QuickViewParams]

    val stack = new MockPanelStack[QuickViewParams](
      pushMock = push,
      updateMock = update,
      popMock = pop,
      peekMock = peek,
      paramsMock = params
    )
  }

  it should "define uiComponent and triggerKey" in {
    //given
    val actions = new MockFileListActions
    val plugin = createQuickViewPlugin(actions)
    
    //when & then
    plugin.uiComponent shouldBe QuickViewPanel
    
    //when & then
    plugin.triggerKey shouldBe "C-q"
  }
  
  it should "remove plugin from left panel when onTrigger" in {
    //given
    val actions = new MockFileListActions
    val plugin = createQuickViewPlugin(actions)
    val leftStack = new Stack
    val rightStack = new Stack
    leftStack.peek.expects().returning(PanelStackItem(plugin(), None, None, None))
    
    //then
    leftStack.pop.expects()

    //when
    plugin.onTrigger(isRight = false, leftStack.stack, rightStack.stack)
  }
  
  it should "remove plugin from right panel when onTrigger" in {
    //given
    val actions = new MockFileListActions
    val plugin = createQuickViewPlugin(actions)
    val leftStack = new Stack
    val rightStack = new Stack
    leftStack.peek.expects().returning(PanelStackItem("other".asInstanceOf[ReactClass], None, None, None))
    rightStack.peek.expects().returning(PanelStackItem(plugin(), None, None, None))
    
    //then
    rightStack.pop.expects()

    //when
    plugin.onTrigger(isRight = false, leftStack.stack, rightStack.stack)
  }
  
  it should "add plugin to left panel when onTrigger" in {
    //given
    val actions = new MockFileListActions
    val plugin = createQuickViewPlugin(actions)
    val leftStack = new Stack
    val rightStack = new Stack
    leftStack.peek.expects().returning(PanelStackItem("other1".asInstanceOf[ReactClass], None, None, None))
    rightStack.peek.expects().returning(PanelStackItem("other2".asInstanceOf[ReactClass], None, None, None))
    
    //then
    leftStack.push.expects(PanelStackItem(plugin(), None, None, Some(QuickViewParams())))

    //when
    plugin.onTrigger(isRight = true, leftStack.stack, rightStack.stack)
  }
  
  it should "add plugin to right panel when onTrigger" in {
    //given
    val actions = new MockFileListActions
    val plugin = createQuickViewPlugin(actions)
    val leftStack = new Stack
    val rightStack = new Stack
    leftStack.peek.expects().returning(PanelStackItem("other1".asInstanceOf[ReactClass], None, None, None))
    rightStack.peek.expects().returning(PanelStackItem("other2".asInstanceOf[ReactClass], None, None, None))
    
    //then
    rightStack.push.expects(PanelStackItem(plugin(), None, None, Some(QuickViewParams())))

    //when
    plugin.onTrigger(isRight = false, leftStack.stack, rightStack.stack)
  }
  
  it should "map state to props" in {
    //given
    val actions = new MockFileListActions
    val props = mock[Props[Unit]]
    val plugin = createQuickViewPlugin(actions)
    val dispatch = mock[Dispatch]
    val fileListsState = FileListsState()
    val fileListsStateMock = mockFunction[FileListsStateDef]
    val state = new FileListsGlobalState {
      override def fileListsState: FileListsStateDef = fileListsStateMock()
    }
    fileListsStateMock.expects().returning(fileListsState)

    //when
    val result = plugin.mapStateToProps(dispatch, state, props)
    
    //then
    inside(result) { case QuickViewPanelProps(disp, resActions, data) =>
      disp shouldBe dispatch
      resActions shouldBe resActions
      data shouldBe fileListsState
    }
  }
}
