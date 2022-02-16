package farjs.filelist.fs

import farjs.filelist._
import farjs.filelist.fs.FSPanel._
import farjs.filelist.stack.PanelStackSpec.withContext
import farjs.filelist.stack.{PanelStack, PanelStackItem}
import scommons.react.test._

import scala.scalajs.js

class FSPanelSpec extends TestSpec with TestRendererUtils {

  FSPanel.fileListPanelComp = mockUiComponent("FileListPanel")

  it should "render component and update isActive" in {
    //given
    val dispatch = mockFunction[Any, Any]
    val actions = new MockFileListActions
    val state = FileListState()
    state.isActive shouldBe false
    
    var stackState = List[PanelStackItem[FileListState]](
      PanelStackItem[FileListState](FSPanel(), Some(dispatch), Some(actions), Some(state))
    )
    val stack = new PanelStack(isActive = true, stackState, { f =>
      stackState = f(stackState).asInstanceOf[List[PanelStackItem[FileListState]]]
    }: js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]] => Unit)

    //when & then
    val renderer = createTestRenderer(
      withContext(<(FSPanel())()(), stack = stack)
    )
    assertTestComponent(renderer.root.children(0), fileListPanelComp) {
      case FileListPanelProps(resDispatch, resActions, resState) =>
        resDispatch shouldBe dispatch
        resActions shouldBe actions
        resState shouldBe state
    }

    //then
    inside(stackState.head) { case PanelStackItem(_, resDispatch, resActions, resState) =>
      resDispatch shouldBe Some(dispatch)
      resActions shouldBe Some(actions)
      resState shouldBe Some(state.copy(isActive = true))
    }
    val updaterMock = mockFunction[js.Function1[List[PanelStackItem[_]], List[PanelStackItem[_]]], Unit]
    updaterMock.expects(*).never()
    
    //when & then
    TestRenderer.act { () =>
      renderer.update(
        withContext(<(FSPanel())()(), stack = new PanelStack(isActive = true, stackState, updaterMock))
      )
    }
    assertTestComponent(renderer.root.children(0), fileListPanelComp) {
      case FileListPanelProps(resDispatch, resActions, resState) =>
        resDispatch shouldBe dispatch
        resActions shouldBe actions
        resState shouldBe state.copy(isActive = true)
    }

    //then
    inside(stackState.head) { case PanelStackItem(_, resDispatch, resActions, resState) =>
      resDispatch shouldBe Some(dispatch)
      resActions shouldBe Some(actions)
      resState shouldBe Some(state.copy(isActive = true))
    }
  }
}
