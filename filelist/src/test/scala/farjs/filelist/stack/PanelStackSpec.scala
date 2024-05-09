package farjs.filelist.stack

import farjs.filelist.stack.PanelStackSpec.{TestParams, assertPanelStackItem, assertPanelStackItems}
import org.scalactic.source.Position
import org.scalatest.{Assertion, Succeeded}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import scommons.nodejs.test.TestSpec
import scommons.react._

import scala.scalajs.js

class PanelStackSpec extends TestSpec {

  it should "push new component when push" in {
    //given
    val updater = mockFunction[js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]], Unit]
    val stack = new PanelStack(isActive = false, js.Array(), updater)
    val data = js.Array[PanelStackItem[_]](
      PanelStackItem("existing comp".asInstanceOf[ReactClass])
    )
    val newItem = new PanelStackItem("new comp".asInstanceOf[ReactClass], js.undefined, js.undefined, TestParams("test name"))
    
    var result: js.Array[PanelStackItem[_]] = null
    updater.expects(*).onCall { updateFn: js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]] =>
      //when
      result = updateFn(data)
    }
    
    //when
    stack.push(newItem)
    
    //then
    result.toList shouldBe newItem :: data.toList
  }
  
  it should "do nothing if empty stack data when update" in {
    //given
    val updater = mockFunction[js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]], Unit]
    val stack = new PanelStack(isActive = false, js.Array(), updater)
    val params = TestParams("test name")
    val data = js.Array[PanelStackItem[_]]()
    
    var result: js.Array[PanelStackItem[_]] = null
    updater.expects(*).onCall { updateFn: js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]] =>
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
    val updater = mockFunction[js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]], Unit]
    val stack = new PanelStack(isActive = false, js.Array(), updater)
    val params = TestParams("test name")
    val top = PanelStackItem[TestParams]("top comp".asInstanceOf[ReactClass])
    val other = PanelStackItem("other comp".asInstanceOf[ReactClass])
    val data = js.Array[PanelStackItem[_]](top, other)
    
    var result: js.Array[PanelStackItem[_]] = null
    updater.expects(*).onCall { updateFn: js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]] =>
      //when
      result = updateFn(data)
    }
    
    //when
    stack.update[TestParams](_.withState(params))
    
    //then
    assertPanelStackItems(result.toList, List(PanelStackItem.copy(top)(state = params), other))
  }
  
  it should "update item when updateFor" in {
    //given
    val updater = mockFunction[js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]], Unit]
    val stack = new PanelStack(isActive = false, js.Array(), updater)
    val params = TestParams("test name")
    val top = PanelStackItem[TestParams]("top comp".asInstanceOf[ReactClass])
    val component = "other comp".asInstanceOf[ReactClass]
    val other = PanelStackItem[TestParams](component)
    val data = js.Array[PanelStackItem[_]](top, other)
    
    var result: js.Array[PanelStackItem[_]] = null
    updater.expects(*).onCall { updateFn: js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]] =>
      //when
      result = updateFn(data)
    }
    
    //when
    stack.updateFor[TestParams](component)(_.withState(params))
    
    //then
    assertPanelStackItems(result.toList, List(top, PanelStackItem.copy(other)(state = params)))
  }
  
  it should "remove top component when pop" in {
    //given
    val updater = mockFunction[js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]], Unit]
    val stack = new PanelStack(isActive = false, js.Array(), updater)
    val other = PanelStackItem("other comp".asInstanceOf[ReactClass])
    val data = js.Array[PanelStackItem[_]](
      PanelStackItem[TestParams]("top comp".asInstanceOf[ReactClass]),
      other
    )
    
    var result: js.Array[PanelStackItem[_]] = null
    updater.expects(*).onCall { updateFn: js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]] =>
      //when
      result = updateFn(data)
    }
    
    //when
    stack.pop()
    
    //then
    assertPanelStackItems(result.toList, List(other))
  }
  
  it should "not remove last item when pop" in {
    //given
    val updater = mockFunction[js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]], Unit]
    val stack = new PanelStack(isActive = false, js.Array(), updater)
    val data = js.Array[PanelStackItem[_]](
      PanelStackItem("top comp".asInstanceOf[ReactClass])
    )
    
    var result: js.Array[PanelStackItem[_]] = null
    updater.expects(*).onCall { updateFn: js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]] =>
      //when
      result = updateFn(data)
    }
    
    //when
    stack.pop()
    
    //then
    assertPanelStackItems(result.toList, data.toList)
  }
  
  it should "remove all except last item when clear" in {
    //given
    val updater = mockFunction[js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]], Unit]
    val stack = new PanelStack(isActive = false, js.Array(), updater)
    val other = PanelStackItem("other comp".asInstanceOf[ReactClass])
    val data = js.Array[PanelStackItem[_]](
      PanelStackItem[TestParams]("top comp1".asInstanceOf[ReactClass]),
      PanelStackItem[TestParams]("top comp2".asInstanceOf[ReactClass]),
      other
    )
    
    var result: js.Array[PanelStackItem[_]] = null
    updater.expects(*).onCall { updateFn: js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]] =>
      //when
      result = updateFn(data)
    }
    
    //when
    stack.clear()
    
    //then
    assertPanelStackItems(result.toList, List(other))
  }

  it should "not remove last item when clear" in {
    //given
    val updater = mockFunction[js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]], Unit]
    val stack = new PanelStack(isActive = false, js.Array(), updater)
    val data = js.Array[PanelStackItem[_]](
      PanelStackItem("top comp".asInstanceOf[ReactClass])
    )

    var result: js.Array[PanelStackItem[_]] = null
    updater.expects(*).onCall { updateFn: js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]] =>
      //when
      result = updateFn(data)
    }

    //when
    stack.clear()

    //then
    assertPanelStackItems(result.toList, data.toList)
  }

  it should "return top item when peek" in {
    //given
    val params = TestParams(name = "test")
    val top = PanelStackItem("top comp".asInstanceOf[ReactClass], js.undefined, js.undefined, params)
    val other = PanelStackItem("other comp".asInstanceOf[ReactClass])
    val stack = new PanelStack(isActive = false, js.Array(top, other), null)
    
    //when
    val result = stack.peek
    
    //then
    assertPanelStackItem(result, top)
  }
  
  it should "return last item when peekLast" in {
    //given
    val params = TestParams(name = "test")
    val top = PanelStackItem("top comp".asInstanceOf[ReactClass], js.undefined, js.undefined, params)
    val other = PanelStackItem("other comp".asInstanceOf[ReactClass])
    val stack = new PanelStack(isActive = false, js.Array(top, other), null)
    
    //when
    val result = stack.peekLast
    
    //then
    assertPanelStackItem(result, other)
  }
  
  it should "return params of top item when params" in {
    //given
    val params = TestParams(name = "test")
    val top = PanelStackItem("top comp".asInstanceOf[ReactClass], js.undefined, js.undefined, params)
    val other = PanelStackItem("other comp".asInstanceOf[ReactClass])
    val stack = new PanelStack(isActive = false, js.Array(top, other), null)
    
    //when
    val result = stack.params[TestParams]
    
    //then
    result shouldBe params
  }
  
  it should "return null if None when params" in {
    //given
    val top = PanelStackItem[TestParams]("top comp".asInstanceOf[ReactClass])
    val stack = new PanelStack(isActive = false, js.Array(top), null)
    
    //when
    val result = stack.params[TestParams]
    
    //then
    result shouldBe null
  }
}

object PanelStackSpec {

  private case class TestParams(name: String)

  def assertPanelStackItems(result: List[PanelStackItem[_]], expected: List[PanelStackItem[_]])(implicit position: Position): Assertion = {
    result.size shouldBe expected.size
    result.zip(expected).foreach { case (res, exp) =>
      assertPanelStackItem(res, exp)
    }
    Succeeded
  }

  def assertPanelStackItem(result: PanelStackItem[_], expected: PanelStackItem[_])(implicit position: Position): Assertion = {
    result.component shouldBe expected.component
    result.dispatch shouldBe expected.dispatch
    result.actions shouldBe expected.actions
    result.state shouldBe expected.state
  }
}
