package farjs.fs

import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack._
import farjs.filelist.{FileListState, MockFileListActions}
import org.scalatest.Succeeded
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass

import scala.concurrent.Future
import scala.scalajs.js

class FSPluginSpec extends AsyncTestSpec {

  it should "define triggerKeys" in {
    //when & then
    FSPlugin.triggerKeys.toList shouldBe List("M-l", "M-r", "M-h", "C-d")
  }

  it should "initialize dispatch, actions and state when init" in {
    //given
    val reducer = mockFunction[FileListState, Any, FileListState]
    val plugin = new FSPlugin(reducer)
    val parentDispatch = mockFunction[Any, Any]
    val item = PanelStackItem[FileListState](plugin.component, None, None, None)
    var stackData = List[PanelStackItem[_]](item)
    val stack = new PanelStack(isActive = true, stackData, { f =>
      stackData = f(stackData)
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)
    
    //when
    plugin.init(parentDispatch, stack)
    
    //then
    inside(stackData.head) { case PanelStackItem(component, Some(dispatch), actions, resState) =>
      component shouldBe plugin.component
      actions shouldBe Some(FSFileListActions)
      val currState = inside(resState) {
        case Some(state: FileListState) =>
          state shouldBe FileListState(currDir = state.currDir, isActive = true)
          state
      }
      
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
      inside(stackData.head) {
        case PanelStackItem(component, Some(resDispatch), resActions, resState) =>
          component shouldBe plugin.component
          resDispatch should be theSameInstanceAs dispatch
          resActions should be theSameInstanceAs actions
          resState shouldBe Some(updatedState)
      }
    }
  }

  it should "return None/Some if non-/trigger key when onKeyTrigger" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem("item 1")
    )))
    val leftStack = new PanelStack(isActive = true, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(state))
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, List(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], Some(dispatch), Some(actions), Some(state))
    ), updater = null)
    val stacks = WithPanelStacksProps(leftStack, null, rightStack, null)

    //when & then
    Future.sequence(Seq(
      FSPlugin.onKeyTrigger("test_key", stacks).map(_ shouldBe None),
      FSPlugin.onKeyTrigger("M-l", stacks).map(_ should not be None)
    )).map(_ => Succeeded)
  }

  it should "return Some(ui) if trigger key when createUi" in {
    //when & then
    inside(FSPlugin.createUi("M-l")) { case Some(FSPluginUi(Some(true), false, false)) => }
    inside(FSPlugin.createUi("M-r")) { case Some(FSPluginUi(Some(false), false, false)) => }
    inside(FSPlugin.createUi("M-h")) { case Some(FSPluginUi(None, true, false)) => }
    inside(FSPlugin.createUi("C-d")) { case Some(FSPluginUi(None, false, true)) => }
    Succeeded
  }
}
