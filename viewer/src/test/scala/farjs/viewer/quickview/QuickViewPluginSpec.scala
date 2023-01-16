package farjs.viewer.quickview

import farjs.filelist.stack._
import scommons.react.ReactClass
import scommons.react.test.TestSpec

class QuickViewPluginSpec extends TestSpec {
  
  //noinspection TypeAnnotation
  class Stack(isActive: Boolean = false) {
    val push = mockFunction[PanelStackItem[QuickViewParams], Unit]
    val pop = mockFunction[Unit]
    val peek = mockFunction[PanelStackItem[QuickViewParams]]

    val stack = new MockPanelStack[QuickViewParams](
      isActive = isActive,
      pushMock = push,
      popMock = pop,
      peekMock = peek
    )
  }

  it should "define triggerKeys" in {
    //when & then
    QuickViewPlugin.triggerKeys.toList shouldBe List("C-q")
  }
  
  it should "remove plugin from left panel when onKeyTrigger" in {
    //given
    val leftStack = new Stack(isActive = true)
    val rightStack = new Stack
    val stacks = WithPanelStacksProps(leftStack.stack, null, rightStack.stack, null)
    leftStack.peek.expects().returning(PanelStackItem(QuickViewPanel(), None, None, None))
    
    //then
    leftStack.pop.expects()

    //when
    QuickViewPlugin.onKeyTrigger("", stacks) shouldBe None
  }
  
  it should "remove plugin from right panel when onKeyTrigger" in {
    //given
    val leftStack = new Stack(isActive = true)
    val rightStack = new Stack
    val stacks = WithPanelStacksProps(leftStack.stack, null, rightStack.stack, null)
    leftStack.peek.expects().returning(PanelStackItem("other".asInstanceOf[ReactClass], None, None, None))
    rightStack.peek.expects().returning(PanelStackItem(QuickViewPanel(), None, None, None))
    
    //then
    rightStack.pop.expects()

    //when
    QuickViewPlugin.onKeyTrigger("", stacks) shouldBe None
  }
  
  it should "add plugin to left panel when onKeyTrigger" in {
    //given
    val leftStack = new Stack
    val rightStack = new Stack(isActive = true)
    val stacks = WithPanelStacksProps(leftStack.stack, null, rightStack.stack, null)
    leftStack.peek.expects().returning(PanelStackItem("other1".asInstanceOf[ReactClass], None, None, None))
    rightStack.peek.expects().returning(PanelStackItem("other2".asInstanceOf[ReactClass], None, None, None))
    
    //then
    leftStack.push.expects(PanelStackItem(QuickViewPanel(), None, None, Some(QuickViewParams())))

    //when
    QuickViewPlugin.onKeyTrigger("", stacks) shouldBe None
  }
  
  it should "add plugin to right panel when onKeyTrigger" in {
    //given
    val leftStack = new Stack(isActive = true)
    val rightStack = new Stack
    val stacks = WithPanelStacksProps(leftStack.stack, null, rightStack.stack, null)
    leftStack.peek.expects().returning(PanelStackItem("other1".asInstanceOf[ReactClass], None, None, None))
    rightStack.peek.expects().returning(PanelStackItem("other2".asInstanceOf[ReactClass], None, None, None))
    
    //then
    rightStack.push.expects(PanelStackItem(QuickViewPanel(), None, None, Some(QuickViewParams())))

    //when
    QuickViewPlugin.onKeyTrigger("", stacks) shouldBe None
  }
}
