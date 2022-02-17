package farjs.filelist.fs

import farjs.filelist.FileListState
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import scommons.nodejs.test.TestSpec

import scala.scalajs.js

class FSPluginSpec extends TestSpec {

  it should "return correct component" in {
    //when & then
    FSPlugin.component shouldBe FSPanel()
  }

  it should "initialize dispatch, actions and state when init" in {
    //given
    val reducer = mockFunction[FileListState, Any, FileListState]
    val plugin = new FSPlugin(reducer)
    val parentDispatch = mockFunction[Any, Any]
    val item = PanelStackItem[FileListState](FSPlugin.component, None, None, None)
    var stackData = List[PanelStackItem[_]](item)
    val stack = new PanelStack(isActive = true, stackData, { f =>
      stackData = f(stackData)
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)
    
    //when
    plugin.init(parentDispatch, stack)
    
    //then
    inside(stackData.head) { case PanelStackItem(component, Some(dispatch), actions, state) =>
      val currState = FileListState(isActive = true)
      component shouldBe FSPlugin.component
      actions shouldBe Some(FSFileListActions)
      state shouldBe Some(currState)
      
      //given
      val action = "test action"
      val updatedState = FileListState()
      currState should not be updatedState
      
      //then
      reducer.expects(currState, action).returning(updatedState)
      parentDispatch.expects(action)
      
      //when
      dispatch(action)
      
      //then
      inside(stackData.head) { case PanelStackItem(component, Some(resDispatch), resActions, resState) =>
        component shouldBe FSPlugin.component
        resDispatch should be theSameInstanceAs dispatch
        resActions should be theSameInstanceAs actions
        resState shouldBe Some(updatedState)
      }
    }
  }
}
