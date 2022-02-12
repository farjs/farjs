package farjs.filelist.fs

import farjs.filelist._
import farjs.filelist.fs.FSPanel._
import farjs.filelist.stack.PanelStackSpec.withContext
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import scommons.react.test._

import scala.scalajs.js

class FSPanelSpec extends TestSpec with TestRendererUtils {

  FSPanel.fileListPanelComp = mockUiComponent("FileListPanel")

  it should "render component with left state" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val props = FSPanelProps(dispatch, actions, FileListsState())
    val state = props.data.left
    val isRight = false
    
    var stackState = List[PanelStackItem[FileListState]](
      PanelStackItem[FileListState](FSPanel(), None, None, None)
    )
    val stack = new PanelStack(isActive = false, stackState, { f =>
      stackState = f(stackState).asInstanceOf[List[PanelStackItem[FileListState]]]
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)

    //when
    val result = testRender(
      withContext(<(FSPanel())(^.wrapped := props)(), isRight, stack = stack)
    )

    //then
    inside(stackState.head) { case PanelStackItem(_, resDispatch, resActions, resState) =>
      resDispatch shouldBe Some(dispatch)
      resActions shouldBe Some(actions)
      resState shouldBe Some(state)
    }
    
    assertTestComponent(result, fileListPanelComp) {
      case FileListPanelProps(resDispatch, resActions, resState) =>
        resDispatch shouldBe dispatch
        resActions shouldBe actions
        resState shouldBe state
    }
  }

  it should "render component with right state" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val props = FSPanelProps(dispatch, actions, FileListsState())
    val state = props.data.right
    val isRight = true
    
    var stackState = List[PanelStackItem[FileListState]](
      PanelStackItem[FileListState](FSPanel(), None, None, None)
    )
    val stack = new PanelStack(isActive = false, stackState, { f =>
      stackState = f(stackState).asInstanceOf[List[PanelStackItem[FileListState]]]
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)

    //when
    val result = testRender(
      withContext(<(FSPanel())(^.wrapped := props)(), isRight, stack = stack)
    )

    //then
    inside(stackState.head) { case PanelStackItem(_, resDispatch, resActions, resState) =>
      resDispatch shouldBe Some(dispatch)
      resActions shouldBe Some(actions)
      resState shouldBe Some(state)
    }

    assertTestComponent(result, fileListPanelComp) {
      case FileListPanelProps(resDispatch, resActions, resState) =>
        resDispatch shouldBe dispatch
        resActions shouldBe actions
        resState shouldBe state
    }
  }
}
