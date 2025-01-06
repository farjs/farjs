package farjs.fs

import farjs.filelist.FileListStateSpec.assertFileListState
import farjs.filelist.api.{FileListDir, FileListItem}
import farjs.filelist.stack._
import farjs.filelist.{FileListState, MockFileListActions}
import org.scalatest.{OptionValues, Succeeded}
import scommons.nodejs.test.AsyncTestSpec
import scommons.react.ReactClass

import scala.concurrent.Future
import scala.scalajs.js

class FSPluginSpec extends AsyncTestSpec with OptionValues {

  it should "define triggerKeys" in {
    //when & then
    FSPlugin.triggerKeys.toList shouldBe List("M-l", "M-r", "M-h", "C-d")
  }

  it should "initialize dispatch, actions and state when init" in {
    //given
    val reducer = mockFunction[FileListState, Any, FileListState]
    val plugin = new FSPlugin(reducer)
    val parentDispatch = mockFunction[js.Any, Unit]
    val item = PanelStackItem[FileListState](plugin.component)
    var stackData = js.Array[PanelStackItem[_]](item)
    val stack = new PanelStack(isActive = true, stackData, { f =>
      stackData = f(stackData)
    }: js.Function1[js.Array[PanelStackItem[_]], js.Array[PanelStackItem[_]]] => Unit)
    
    //when
    plugin.init(parentDispatch, stack)
    
    //then
    inside(stackData.head) { case PanelStackItem(component, dispatch, actions, resState) =>
      component shouldBe plugin.component
      actions shouldBe FSFileListActions
      val currState = inside(resState.toOption) {
        case Some(s) =>
          val state = s.asInstanceOf[FileListState]
          assertFileListState(state, FileListState(currDir = state.currDir))
          state
      }
      
      //given
      val action = "test action"
      val updatedState = FileListState()
      currState should not be updatedState
      
      //then
      reducer.expects(currState, action).returning(updatedState)
      parentDispatch.expects(action.asInstanceOf[js.Any])
      
      //when
      dispatch.toOption.value(action)
      
      //then
      inside(stackData.head) {
        case PanelStackItem(component, resDispatch, resActions, resState) =>
          component shouldBe plugin.component
          resDispatch should be theSameInstanceAs dispatch
          resActions should be theSameInstanceAs actions
          resState shouldBe updatedState
      }
    }
  }

  it should "return None/Some if non-/trigger key when onKeyTrigger" in {
    //given
    val dispatch: js.Function1[js.Any, Unit] = mockFunction[js.Any, Unit]
    val actions = new MockFileListActions
    val state = FileListState(currDir = FileListDir("/sub-dir", isRoot = false, items = js.Array(
      FileListItem("item 1")
    )))
    val leftStack = new PanelStack(isActive = true, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch, actions, state)
    ), updater = null)

    val rightStack = new PanelStack(isActive = false, js.Array(
      PanelStackItem("fsComp".asInstanceOf[ReactClass], dispatch, actions, state)
    ), updater = null)
    val stacks = WithStacksProps(WithStacksData(leftStack, null), WithStacksData(rightStack, null))

    //when & then
    Future.sequence(Seq(
      FSPlugin.onKeyTrigger("test_key", stacks).toFuture.map(_ shouldBe js.undefined),
      FSPlugin.onKeyTrigger("M-l", stacks).toFuture.map(_ should not be js.undefined)
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
