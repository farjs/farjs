package farjs.viewer.quickview

import farjs.filelist.stack._
import org.scalatest.OptionValues
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass

import scala.scalajs.js

class QuickViewPluginSpec extends AsyncTestSpec with OptionValues {
  
  it should "define triggerKeys" in {
    //when & then
    QuickViewPlugin.triggerKeys.toList shouldBe List("C-q")
  }
  
  it should "remove plugin from left panel when onKeyTrigger" in {
    //given
    var stackState = js.Array[PanelStackItem[_]](
      PanelStackItem(QuickViewPanel(), js.undefined, js.undefined, QuickViewParams()),
      PanelStackItem("fsComp".asInstanceOf[ReactClass], js.undefined, js.undefined, js.undefined)
    )
    val leftStack = new PanelStack(isActive = true, stackState, { f =>
      stackState = f(stackState)
    }: js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]] => Unit)
    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], js.undefined, js.undefined, js.undefined)
    ), null)
    val stacks = WithStacksProps(WithStacksData(leftStack, null), WithStacksData(rightStack, null))

    //when
    QuickViewPlugin.onKeyTrigger("", stacks).toFuture.map(_ shouldBe js.undefined)

    //then
    stackState.length shouldBe 1
    stackState.head.component shouldBe "fsComp"
  }
  
  it should "remove plugin from right panel when onKeyTrigger" in {
    //given
    var stackState = js.Array[PanelStackItem[_]](
      PanelStackItem(QuickViewPanel(), js.undefined, js.undefined, QuickViewParams()),
      PanelStackItem("fsComp".asInstanceOf[ReactClass], js.undefined, js.undefined, js.undefined)
    )
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], js.undefined, js.undefined, js.undefined)
    ), null)
    val rightStack = new PanelStack(isActive = false, stackState, { f =>
      stackState = f(stackState)
    }: js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]] => Unit)
    val stacks = WithStacksProps(WithStacksData(leftStack, null), WithStacksData(rightStack, null))

    //when
    QuickViewPlugin.onKeyTrigger("", stacks).toFuture.map(_ shouldBe js.undefined)

    //then
    stackState.length shouldBe 1
    stackState.head.component shouldBe "fsComp"
  }
  
  it should "add plugin to left panel when onKeyTrigger" in {
    //given
    var stackState = js.Array[PanelStackItem[_]](
      PanelStackItem("fsComp".asInstanceOf[ReactClass], js.undefined, js.undefined, js.undefined)
    )
    val leftStack = new PanelStack(isActive = false, stackState, { f =>
      stackState = f(stackState)
    }: js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]] => Unit)
    val rightStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], js.undefined, js.undefined, js.undefined)
    ), null)
    val stacks = WithStacksProps(WithStacksData(leftStack, null), WithStacksData(rightStack, null))
    
    //when
    QuickViewPlugin.onKeyTrigger("", stacks).toFuture.map(_ shouldBe js.undefined)
    
    //then
    stackState.length shouldBe 2
    stackState.head.component shouldBe QuickViewPanel()
    stackState.head.state.toOption.value shouldBe QuickViewParams()
  }
  
  it should "add plugin to right panel when onKeyTrigger" in {
    //given
    var stackState = js.Array[PanelStackItem[_]](
      PanelStackItem("fsComp".asInstanceOf[ReactClass], js.undefined, js.undefined, js.undefined)
    )
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], js.undefined, js.undefined, js.undefined)
    ), null)
    val rightStack = new PanelStack(isActive = false, stackState, { f =>
      stackState = f(stackState)
    }: js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]] => Unit)
    val stacks = WithStacksProps(WithStacksData(leftStack, null), WithStacksData(rightStack, null))

    //when
    QuickViewPlugin.onKeyTrigger("", stacks).toFuture.map(_ shouldBe js.undefined)

    //then
    stackState.length shouldBe 2
    stackState.head.component shouldBe QuickViewPanel()
    stackState.head.state.toOption.value shouldBe QuickViewParams()
  }
}
