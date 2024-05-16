package farjs.viewer.quickview

import farjs.filelist.stack._
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass

import scala.scalajs.js

class QuickViewPluginSpec extends AsyncTestSpec {
  
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
    val stacks = PanelStacks(PanelStackData(leftStack.stack, null), PanelStackData(rightStack.stack, null))
    leftStack.peek.expects().returning(PanelStackItem(QuickViewPanel()))
    
    //then
    leftStack.pop.expects()

    //when
    QuickViewPlugin.onKeyTrigger("", stacks).toFuture.map(_ shouldBe js.undefined)
  }
  
  it should "remove plugin from right panel when onKeyTrigger" in {
    //given
    val leftStack = new Stack(isActive = true)
    val rightStack = new Stack
    val stacks = PanelStacks(PanelStackData(leftStack.stack, null), PanelStackData(rightStack.stack, null))
    leftStack.peek.expects().returning(PanelStackItem("other".asInstanceOf[ReactClass]))
    rightStack.peek.expects().returning(PanelStackItem(QuickViewPanel()))
    
    //then
    rightStack.pop.expects()

    //when
    QuickViewPlugin.onKeyTrigger("", stacks).toFuture.map(_ shouldBe js.undefined)
  }
  
  it should "add plugin to left panel when onKeyTrigger" in {
    //given
    val leftStack = new Stack
    val rightStack = new Stack(isActive = true)
    val stacks = PanelStacks(PanelStackData(leftStack.stack, null), PanelStackData(rightStack.stack, null))
    leftStack.peek.expects().returning(PanelStackItem("other1".asInstanceOf[ReactClass]))
    rightStack.peek.expects().returning(PanelStackItem("other2".asInstanceOf[ReactClass]))
    
    //then
    leftStack.push.expects(*).onCall { resItem: PanelStackItem[_] =>
      PanelStackSpec.assertPanelStackItem(resItem, PanelStackItem(QuickViewPanel(), js.undefined, js.undefined, QuickViewParams()))
      ()
    }

    //when
    QuickViewPlugin.onKeyTrigger("", stacks).toFuture.map(_ shouldBe js.undefined)
  }
  
  it should "add plugin to right panel when onKeyTrigger" in {
    //given
    val leftStack = new Stack(isActive = true)
    val rightStack = new Stack
    val stacks = PanelStacks(PanelStackData(leftStack.stack, null), PanelStackData(rightStack.stack, null))
    leftStack.peek.expects().returning(PanelStackItem("other1".asInstanceOf[ReactClass]))
    rightStack.peek.expects().returning(PanelStackItem("other2".asInstanceOf[ReactClass]))
    
    //then
    rightStack.push.expects(*).onCall { resItem: PanelStackItem[_] =>
      PanelStackSpec.assertPanelStackItem(resItem, PanelStackItem(QuickViewPanel(), js.undefined, js.undefined, QuickViewParams()))
      ()
    }

    //when
    QuickViewPlugin.onKeyTrigger("", stacks).toFuture.map(_ shouldBe js.undefined)
  }
}
