package farjs.filelist.quickview

import farjs.filelist.stack.{MockPanelStack, PanelStackItem}
import scommons.react.ReactClass
import scommons.react.test.TestSpec

class QuickViewPluginSpec extends TestSpec {
  
  //noinspection TypeAnnotation
  class Stack(isActive: Boolean = false) {
    val push = mockFunction[PanelStackItem[QuickViewParams], Unit]
    val update = mockFunction[PanelStackItem[QuickViewParams] => PanelStackItem[QuickViewParams], Unit]
    val pop = mockFunction[Unit]
    val peek = mockFunction[PanelStackItem[QuickViewParams]]
    val params = mockFunction[QuickViewParams]

    val stack = new MockPanelStack[QuickViewParams](
      isActive = isActive,
      pushMock = push,
      updateMock = update,
      popMock = pop,
      peekMock = peek,
      paramsMock = params
    )
  }

  it should "define triggerKey" in {
    //when & then
    QuickViewPlugin.triggerKey shouldBe Some("C-q")
  }
  
  it should "remove plugin from left panel when onTrigger" in {
    //given
    val leftStack = new Stack(isActive = true)
    val rightStack = new Stack
    leftStack.peek.expects().returning(PanelStackItem(QuickViewPanel(), None, None, None))
    
    //then
    leftStack.pop.expects()

    //when
    QuickViewPlugin.onKeyTrigger(isRight = false, leftStack.stack, rightStack.stack)
  }
  
  it should "remove plugin from right panel when onTrigger" in {
    //given
    val leftStack = new Stack(isActive = true)
    val rightStack = new Stack
    leftStack.peek.expects().returning(PanelStackItem("other".asInstanceOf[ReactClass], None, None, None))
    rightStack.peek.expects().returning(PanelStackItem(QuickViewPanel(), None, None, None))
    
    //then
    rightStack.pop.expects()

    //when
    QuickViewPlugin.onKeyTrigger(isRight = false, leftStack.stack, rightStack.stack)
  }
  
  it should "add plugin to left panel when onTrigger" in {
    //given
    val leftStack = new Stack
    val rightStack = new Stack(isActive = true)
    leftStack.peek.expects().returning(PanelStackItem("other1".asInstanceOf[ReactClass], None, None, None))
    rightStack.peek.expects().returning(PanelStackItem("other2".asInstanceOf[ReactClass], None, None, None))
    
    //then
    leftStack.push.expects(PanelStackItem(QuickViewPanel(), None, None, Some(QuickViewParams())))

    //when
    QuickViewPlugin.onKeyTrigger(isRight = true, leftStack.stack, rightStack.stack)
  }
  
  it should "add plugin to right panel when onTrigger" in {
    //given
    val leftStack = new Stack
    val rightStack = new Stack(isActive = true)
    leftStack.peek.expects().returning(PanelStackItem("other1".asInstanceOf[ReactClass], None, None, None))
    rightStack.peek.expects().returning(PanelStackItem("other2".asInstanceOf[ReactClass], None, None, None))
    
    //then
    rightStack.push.expects(PanelStackItem(QuickViewPanel(), None, None, Some(QuickViewParams())))

    //when
    QuickViewPlugin.onKeyTrigger(isRight = false, leftStack.stack, rightStack.stack)
  }
}
